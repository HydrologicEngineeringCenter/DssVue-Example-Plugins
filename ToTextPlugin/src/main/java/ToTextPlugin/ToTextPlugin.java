import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.util.*;
import java.io.*;

import hec.dssgui.*;
import hec.io.TimeSeriesContainer;
import hec.heclib.util.HecTime;
import hec.heclib.util.HecDoubleArray;

public class ToTextPlugin
{
    public static void main(Object[] args)
    {
        final ToTextPlugin plugin = new ToTextPlugin();
        final ListSelection listSelection = (ListSelection) args[0];

        JMenuItem button = new JMenuItem("To Text File...");
        button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                plugin.process(listSelection);
            }
        });
        listSelection.getToolBar().add(button);
    }

    protected void process(ListSelection listSelection)
    {
        //  Reads data from DSS and returns it in data containers in a list of lists
        List[] dataList = listSelection.getSelectedDataContainers();
        if (dataList == null) {
            JOptionPane.showMessageDialog(listSelection, "No records selected",
                    "Cannot read data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // The first list contains a list of TimeSeriesContainers
        // The second is PairedDataContainers, third is text, fourth is gridded
        List tsContainers = dataList[0];
        if ( (tsContainers != null) && (tsContainers.size() > 0)) {
            processData(listSelection, tsContainers);
        }
        else {
            JOptionPane.showMessageDialog(listSelection, "No time series data read",
                    "No data found", JOptionPane.WARNING_MESSAGE);
        }
    }


    protected boolean processData(ListSelection listSelection,
                                  List tsContainers) {
        try {
            //  Get the name of the text file to write to.
            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setDialogTitle("Enter file to save data to");
            chooser.setFileFilter(new rma.util.RMAFilenameFilter("txt", "*.txt"));
            chooser.showOpenDialog(listSelection);
            java.io.File file = chooser.getSelectedFile();
            if (file == null)
                return false;
            String fileName = file.getAbsolutePath();
            if (fileName.endsWith(".txt") == false)
                fileName = fileName + ".txt";
            PrintWriter textOut = new PrintWriter(new FileWriter(fileName));
            //  Write either as either data in a single column or data sets
            //  in multi-column.  The table software organizes data sets with times.
            boolean tableStyle = true;
            if (tableStyle) {
                writeTableFormat(textOut, tsContainers);
            }
            else {
                writeSingleSets(textOut, tsContainers);
            }
            textOut.close();
            JOptionPane.showMessageDialog(listSelection,
                    tsContainers.size() + " data sets written to " + fileName,
                    "Write Successful", JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(listSelection, e.toString(),
                    "Cannot write to file", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    protected void writeSingleSets(PrintWriter textOut, List tsContainers) {
        HecTime time = new HecTime();
        for (int i = 0; i < tsContainers.size(); i++) {
            TimeSeriesContainer tsc = (TimeSeriesContainer) tsContainers.get(i);
            textOut.println(tsc.fullName);
            //  HecDouble will take care of missing data and the precision
            HecDoubleArray values = new HecDoubleArray();
            values.set(tsc.values);
            values.setPrecision(tsc.precision);
            for (int j = 0; j < tsc.numberValues; j++) {
                time.set(tsc.times[j]);
                textOut.println(time.toString(4) + ";  " + values.element(j));
            }
        }
    }

    protected void writeTableFormat(PrintWriter textOut, List tsContainers) {
        Table dataTable = new Table(null);
        dataTable.setData(tsContainers, false, 0);
        // UndefinedStyle: 0 = ""; 1 = -901.0; 2 = "M"; 3 = "-M-"
        dataTable.setUndefinedStyle(2);
        //  See HecTime (or heclib juldat) for date styles
        dataTable.setDateStyle(4);
        int numberColumns = dataTable.getColumnCount();
        int numberRows = dataTable.getRowCount();
        int startingRow = dataTable.getNumberHeaderRows();  //  Set to 0 if you want headers also
        for (int i=startingRow; i<numberRows; i++) {
            StringBuffer sb = new StringBuffer();
            for (int j=0; j<numberColumns; j++) {
                sb.append((dataTable.getValueAt(i, j)).toString());
                sb.append("   ");
            }
            textOut.println(sb.toString());
        }
    }
}
