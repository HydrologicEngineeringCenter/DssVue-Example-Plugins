# This is only a temporary fix.  Need something better.
import sys
sys.path.append("C:\Projects\DssVue-Example-Plugins\PythonLauncherPlugin\py")

import matplotlib.pyplot as plt
from matplotlib.image import imsave
import pandas as pd
import seaborn as sns
import numpy as np
from sklearn.preprocessing import StandardScaler, MinMaxScaler, normalize
import datetime
import re

from keras import optimizers, layers
from keras.models import Sequential
from keras.layers import Dense, LeakyReLU
import tensorflow as tf

#from hydroeval import evaluator, nse, rmse, pbias
#from sklearn import metrics

from pydsstools.heclib.dss.HecDss import Open
from pydsstools.heclib.dss import HecDss
from pydsstools.core import TimeSeriesContainer,UNDEFINED, PairedDataContainer

import tkinter as tk
from tkinter import simpledialog,messagebox


#################################################
###These are to setup the popup for data entry###
#################################################


fields = 'DSS File Location. (Example: C:\dssPython-master\machinecode\MissingPrecipData.dss)','Start of Training Data Timeframe(format ddMMMYYYY HHMM):','Stop of Training Data Timeframe(format ddMMMYYYY HHMM):','Start of Second Training Data Timeframe(format ddMMMYYYY HHMM):','Stop of Second Training Data Timeframe(format ddMMMYYYY HHMM):','Start of Missing Data Timeframe(format ddMMMYYYY HHMM):','Stop of Missing Data Timeframe(format ddMMMYYYY HHMM):','Part-B of the DSS file that is Missing Data(ex.:PORT1-REDR-PORTROYALTN):','Enter the units of measurement(mm or Inches):','Enter the measurement type(INST-VAL, INST-CUM, PER-AVER, or PER-CUM):'

def fetch(entries):
    global InputtedInformation
    global FieldInformation 
    for entry in entries:
        field = entry[0]
        text  = entry[1].get()
        FieldInformation.append(field)
        InputtedInformation.append(text)
        print('%s: "%s"' % (field, text)) 

def makeform(root, fields):
    entries = []
    for field in fields:
        row = tk.Frame(root)
        lab = tk.Label(row, width=70, text=field, anchor='w')
        ent = tk.Entry(row)
        row.pack(side=tk.TOP, fill=tk.X, padx=5, pady=5)
        lab.pack(side=tk.LEFT)
        ent.pack(side=tk.RIGHT, expand=tk.YES, fill=tk.X)
        entries.append((field, ent))
    return entries

#if __name__ == '__main__':
if True:
    root = tk.Tk()
    FieldInformation = []
    InputtedInformation = []
    ents = makeform(root, fields)
    root.bind('<Return>', (lambda event, e=ents: fetch(e)))   
    b1 = tk.Button(root, text='Okay',
                  command=(lambda e=ents: fetch(e)))
    b1.pack(side=tk.LEFT, padx=5, pady=5)
    b2 = tk.Button(root, text='Quit', command=root.destroy)
    b2.pack(side=tk.LEFT, padx=5, pady=5)
    root.mainloop()
else:
    print("This program is not functioning correctly.")
    

# the input dialog
dss_file = InputtedInformation[0]
DataForTrainingStart = InputtedInformation[1]
DataForTrainingEnd = InputtedInformation[2]
ScalingDataStart = InputtedInformation[3]
ScalingDataEnd = InputtedInformation[4]
MisingDataPeriodStart = InputtedInformation[5]
MisingDataPeriodEnd = InputtedInformation[6]

############################################################
###Now we parse the DSS file and extract the desired data###
############################################################


# Open up the dss file and look for pathnames that include precip
with Open(dss_file) as fid:
	pathname_pattern = "/*/*/PRECIP/*/*/*/"
	path_list = fid.getPathnameList(pathname_pattern,sort=1)

# This goes through and splits all the options making a list that is
# seperated by the "/" in the pathnames
path_list2 = [l.split(',') for l in ','.join(path_list).split('/')]
# Then we go through and pick "B-part" of the DSSPath names since these are usually unique
path_list3 = path_list2[2::7]

#Go through and find all the unique B-parts in the dss pathnames
ToFindUnique = []
for x in path_list3:
	ToFindUnique.append(tuple(x))

from collections import Counter
FindDuplicateCount = dict(Counter(ToFindUnique))

B_part = list(set(ToFindUnique))

n = 0
FindPathnamesCount = []
for x in B_part:
    FindPathnamesCount.append(list(FindDuplicateCount.values())[n])
    n = n + 1

n = 0
DSSPathID = []
temp = 0
temp2 = 0
for x in B_part:
    temp = FindPathnamesCount[n]    
    DSSPathID.append(path_list[temp2])
    temp2 = temp2 + temp
    n = n + 1

DSSPathID.sort()

n = 0
pathnameList = []
for x in DSSPathID:
    IndexForDoubleDigit = re.search(r"\d\d",DSSPathID[n])
    test_str = DSSPathID[n]
    new_str = test_str[:IndexForDoubleDigit.start()]+test_str[(IndexForDoubleDigit.start()+9):]
    pathnameList.append(new_str)
    n = n + 1

n = 0
Pathnames = []
for B in B_part:
    Pathnames.append(B_part[n][0])
    n = n + 1

#Sorting the pathnames to make sure everything is kept in the same order once the processes start
Pathnames.sort()

fid = HecDss.Open(dss_file)

names = Pathnames

# we have to now replace -901 to missing values in the dataframe
ts = fid.read_ts(pathnameList[0])
values = ts.values
df = pd.DataFrame(ts.pytimes, columns=['Dates'])
df.set_index('Dates', inplace=True)
i = 0
for path in pathnameList:
    ts = fid.read_ts(path)
    df[names[i]] = ts.values
    i += 1

df.loc[(df[names[1]] < 0, names[1])] = np.NaN
file = df.to_csv("TempForMissingData.csv")
fid.close()

# # a problem that needs to be done is inserting values from dataset date and times
dataSet = pd.read_csv("TempForMissingData.csv", index_col=0, parse_dates=True)


#Trying to get headers for later on
ColumnHeaders = tuple()

for col in dataSet.columns: 
    ColumnHeaders = ColumnHeaders+(col,)

#For the setup, we created a new column for the missing data.

ColumnOfInterest = InputtedInformation[7]
ColumnHeaderMissing = ColumnOfInterest
CompletedDataset = ColumnOfInterest+'_Completed'


AllColumns = ColumnHeaders+(CompletedDataset,)

#we create a date column to extract the week number
dataSet['date']=dataSet.index
#apply a lambda function to the whole panda dataframe column
dataSet['week'] = dataSet['date'].apply(lambda x: x.isocalendar()[1])
#drop the date column because we dont need it
del dataSet['date']
#let see our dataframe

#Duplicating the initial data for a second compute later on
dataSet2 = dataSet

###################################
###Prepping the data for the ANN###
###################################

#creation of a correlation plot with seaborn
corr = dataSet.corr()
sns.heatmap(corr, 
            xticklabels=corr.columns.values,
            yticklabels=corr.columns.values)
#Definition of training sets
X_train = dataSet.loc[DataForTrainingStart:DataForTrainingEnd, ColumnHeaders].astype(np.float32).values
y_train = dataSet.loc[DataForTrainingStart:DataForTrainingEnd,ColumnHeaderMissing].astype(np.float32).values 

# This is to scale everything to between [-1,1]
# Define the scaler 
scaler = StandardScaler().fit(X_train)
# Scale the train set
X_train = scaler.transform(X_train)

#############
###The ANN###
#############

model = Sequential()

LeakyRelu = layers.LeakyReLU()
model.add(Dense(12, activation=LeakyRelu, input_shape=(6,)))
layers.LeakyReLU()
model.add(Dense(8, activation=LeakyRelu))
layers.LeakyReLU()
model.add(Dense(1, activation=LeakyRelu))
layers.LeakyReLU()
model.summary()

adam = optimizers.Adam(lr=0.00001, beta_1=0.9, beta_2=0.999, epsilon=1e-8)

model.compile(loss='mean_squared_error',
              optimizer='Nadam',
              metrics=['accuracy'])

                   
model.fit(X_train, y_train,epochs=100,verbose=2, use_multiprocessing=True)

y_pred = model.predict(X_train,use_multiprocessing=True)

plt.plot(dataSet.loc[DataForTrainingStart:DataForTrainingEnd].index,y_pred,label='Predicted')
dataSet[ColumnHeaderMissing].loc[DataForTrainingStart:DataForTrainingEnd].plot()
plt.xlim(DataForTrainingStart, DataForTrainingEnd)
plt.ylim(round(min(dataSet[ColumnHeaderMissing])),round(max(dataSet[ColumnHeaderMissing])))
plt.legend(loc='best')

#Get the prediction for the train set
X_missing = dataSet.loc[MisingDataPeriodStart:MisingDataPeriodEnd,ColumnHeaders].astype(np.float32).values
X_missing_scaling = dataSet2.loc[ScalingDataStart:ScalingDataEnd,ColumnHeaders].astype(np.float32).values

# Define the scaler 
# This is to scale everything to be between [0,1]
min_max_scaler = MinMaxScaler()
scaler = min_max_scaler.fit_transform(X_missing)
scaler = StandardScaler().fit(X_missing)

scaler_scaling = min_max_scaler.fit_transform(X_missing_scaling)
scaler_scaling = StandardScaler().fit(X_missing_scaling)

# Scale the train set
X_missing = scaler.transform(X_missing)
X_missing_scaling = scaler_scaling.transform(X_missing_scaling)

y_missing = model.predict(X_missing)
y_missing = y_missing.reshape([np.count_nonzero(~np.isnan(y_missing))]).tolist()

y_missing_scaling = model.predict(~np.isnan(X_missing_scaling))
y_missing_scaling = y_missing_scaling.reshape([np.count_nonzero(~np.isnan(y_missing_scaling))]).tolist()

X_missing_scaling=dataSet.loc[ScalingDataStart:ScalingDataEnd,[ColumnOfInterest]]
X_missing_scaling[X_missing_scaling <0]=0

for n, i in enumerate(y_missing_scaling):
      if i < 0:
          y_missing_scaling[n] = 0

dataSet[CompletedDataset]=dataSet[ColumnHeaderMissing]
dataSet[CompletedDataset].loc[MisingDataPeriodStart:MisingDataPeriodEnd]=y_missing

# For precip, values below 0 are not possible, so lets replace negative values with 0.
dataSet[dataSet <0]=0

DivDataSet = dataSet[CompletedDataset].loc[ScalingDataStart:ScalingDataEnd].div(y_missing_scaling)
DivDataSetNoNAN = DivDataSet[DivDataSet.notna()]
DivDataSetNoNAN_NoZeros = DivDataSetNoNAN[DivDataSetNoNAN>0]
AverageDifference = DivDataSetNoNAN_NoZeros.mean()
multiplier = AverageDifference

dataSet[CompletedDataset].dtypes
dataSet[CompletedDataset]=dataSet[CompletedDataset].mul(multiplier)

# Plotting all the data for the missing time period
dataSet.loc[MisingDataPeriodStart:MisingDataPeriodEnd,AllColumns].plot(subplots=True, 
                                                   figsize=(24, 18)); plt.legend(loc='best')

# Plotting the missing data for the time period (makes most sense when testing to see the reliability of the model.
dataSet.loc[MisingDataPeriodStart:MisingDataPeriodEnd,[ColumnOfInterest,CompletedDataset]].plot(subplots=False, 
                                                   figsize=(24, 18)); plt.legend(loc='best')

#Another correlation matrix to see how well the missing data is
#correlated to the other data points for the missing data tim eperiod.
corr = dataSet.loc[MisingDataPeriodStart:MisingDataPeriodEnd,AllColumns].corr()
sns.heatmap(corr, 
            xticklabels=corr.columns.values,
            yticklabels=corr.columns.values)

simulated = dataSet.loc[MisingDataPeriodStart:MisingDataPeriodEnd,[CompletedDataset]]
simulated = simulated.to_numpy()
observed = dataSet.loc[MisingDataPeriodStart:MisingDataPeriodEnd,[ColumnOfInterest]]
observed = observed.to_numpy()

###################################
###Moving files back to dss file###
###################################

#This is setup to replace the files in the DSS File, not create a new one.
pathname = [i for i in pathnameList if ColumnOfInterest in i][0]
tsc = TimeSeriesContainer()
tsc.pathname = pathname
tsc.startDateTime = MisingDataPeriodStart
tsc.numberValues = np.count_nonzero(simulated >= 0)
tsc.units = InputtedInformation[8]
tsc.type = InputtedInformation[9]
tsc.interval = 30
tsc.values = simulated.astype(np.float).flatten()
fid = HecDss.Open(dss_file)
fid.put_ts(tsc)
ts = fid.read_ts(pathname)
fid.close()

messagebox.showinfo(title = "Program Complete", message = "Program Successfully Completed")
