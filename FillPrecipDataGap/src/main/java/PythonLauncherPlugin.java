import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.util.*;
import java.io.*;

import hec.dssgui.*;
import hec.io.TimeSeriesContainer;
import hec.heclib.util.HecTime;
import hec.heclib.util.HecDoubleArray;

public class PythonLauncherPlugin
{
    public static void main(Object[] args)
    {
        final PythonLauncherPlugin plugin = new PythonLauncherPlugin();
        final ListSelection listSelection = (ListSelection) args[0];

        JMenuItem menu = new JMenuItem("Launch Python...");

        menu.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                plugin.process(listSelection);
            }
        });

         listSelection.getToolBar().add(menu);
    }

    private void process(ListSelection listSelection)
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


    private boolean processData(ListSelection listSelection,
                                  List tsContainers) {
        try {
            // launch python with hardcoded script
            String command = "C:/project/DssVue-Example-Plugins/PythonLauncherPlugin/py/run-python.bat";

            Runtime run  = Runtime.getRuntime();
            System.out.println(command);
            Process proc = run.exec(command);
            proc.waitFor();
            System.out.println(proc.exitValue());

        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(listSelection, e.toString(),
                    "Cannot write to file", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

}
