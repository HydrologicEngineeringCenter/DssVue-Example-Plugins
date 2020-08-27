import pandas as pd
from pydsstools.heclib.dss import HecDss
from pydsstools.heclib.dss.HecDss import Open
import numpy as np

dss_file = R"C:\project\DssVue-Example-Plugins\PythonLauncherPlugin\py\FlowData.dss"
pathname = "/CUMBERLAND RIVER/BARBOURVILLE/FLOW//30MIN/OBS/"
pathname2 = "/CUMBERLAND RIVER/CUMBERLAND FALLS/FLOW//30MIN/MISSING/"
pathname3 = "/CUMBERLAND RIVER/CUMBERLAND FALLS/FLOW//30MIN/OBS/"
pathname4 = "/CUMBERLAND RIVER/WILLIAMSBURG/FLOW//30MIN/OBS/"

fid = HecDss.Open(dss_file)
pathArr = [pathname, pathname2, pathname3, pathname4]
names = ["data1", "data2", "data3", "data4"]

# we have to now replace -901 to missing values in the dataframe
ts = fid.read_ts(pathname)
values = ts.values
df = pd.DataFrame(ts.pytimes, columns=['Dates'])
df.set_index('Dates', inplace=True)
i = 0
for path in pathArr:
    ts = fid.read_ts(path)
    df[names[i]] = ts.values
    i += 1

df.loc[(df['data2'] < 0, 'data2')] = np.NaN
file = df.to_csv(R"C:\project\DssVue-Example-Plugins\PythonLauncherPlugin\py\flowData.csv")
