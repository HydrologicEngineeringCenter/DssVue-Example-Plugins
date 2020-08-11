import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.util.*;
import java.io.*;

import hec.dssgui.*;
import hec.io.TimeSeriesContainer;
import hec.heclib.util.HecTime;
import hec.heclib.util.HecDoubleArray;

public class Excelv2
{
    public static void main(Object[] args)
    {
        final Excelv2 plugin = new Excelv2();
        final ListSelection listSelection = (ListSelection) args[0];

        JMenuItem menu = new JMenuItem("Excelv2");

        menu.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                plugin.importExcel(listSelection);
            }
        });

         //listSelection.getToolBar().add(menu);
         //listSelection.addToolBarButton(button);
        //listSelection.registerPlugin(ListSelection.TOOLS, menu);
        //listSelection.registerExportPlugin(menu);
        listSelection.registerImportPlugin(null,menu,null);
    }

    private void importExcel(ListSelection listSelection)
    {
        //  ExcelImport.exe file.dss [file.xls]

        try
        {
            // Command to create an external process
            String command = "C:\\Programs\\HEC-DSSVue-v3.0.1.125\\dotnet\\go.bat";
            //System.getProperty("java.library.path");
            // Running the above command
            Runtime run  = Runtime.getRuntime();
            Process proc = run.exec(command);
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }
        listSelection.refreshCatalog();
    }

}
