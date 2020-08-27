import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.*;
import java.io.*;

import hec.dssgui.*;

public class DSSExcel
{
    public static void main(Object[] args)
    {
        final DSSExcel plugin = new DSSExcel();
        final ListSelection listSelection = (ListSelection) args[0];

        JMenuItem importMenuItem = new JMenuItem("Excel Import");
        JMenuItem exportMenuItem = new JMenuItem("Excel Export");

        importMenuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                plugin.importFromExcel(listSelection);
            }
        });

        exportMenuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                plugin.exportToExcel(listSelection);
            }
        });
        listSelection.registerExportPlugin(exportMenuItem);
        listSelection.registerImportPlugin(null,importMenuItem,null);
    }

    private void importFromExcel(ListSelection listSelection)
    {

        try
        {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Excel Files", "xls", "xlsx");
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setFileFilter(filter);
            int result = fileChooser.showOpenDialog(null);
            if (result != JFileChooser.APPROVE_OPTION)
                return;

            String executable = "C:\\Programs\\HEC-DSSVue-v3.0.1.125\\dotnet\\DSSExcel\\DSSExcelCLI.exe";
            String cOption = "import";
            String dOption = listSelection.getDSSFilename();
            String eOption = fileChooser.getSelectedFile().getAbsolutePath();
            List<String> s = new ArrayList<>();
            String command = String.join(" ",
                    executable,
                    "-c " + cOption,
                    "-d " + dOption,
                    "-e " + eOption);
            Runtime run  = Runtime.getRuntime();
            System.out.println(command);
            Process proc = run.exec(command);
            proc.waitFor();
            System.out.println(proc.exitValue());
            listSelection.closeDssFile();
            listSelection.openDSSFile(dOption);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void exportToExcel(ListSelection listSelection)
    {
        try
        {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Excel Files", "xls", "xlsx");
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setFileFilter(filter);
            int result = fileChooser.showSaveDialog(null);
            if (result != JFileChooser.APPROVE_OPTION)
                return;

            DataReferenceSet v = listSelection.getSelectedPathnames();
            String executable = "C:\\Programs\\HEC-DSSVue-v3.0.1.125\\dotnet\\DSSExcel\\DSSExcelCLI.exe";
            String cOption = "export";
            String dOption = listSelection.getDSSFilename();
            String eOption = fileChooser.getSelectedFile().getAbsolutePath();
            List<String> s = new ArrayList<>();
            for (int i = 0; i < v.size(); i++)
                s.add("Export" + (i + 1));
            String sOptions = String.join(",", s);
            List<String> p = new ArrayList<>();
            for (int i = 0; i < v.size(); i++)
                p.add(v.elementAt(i).pathname());
            String pOptions = String.join(",", p);
            pOptions = "\"" + pOptions + "\"";
            String command = String.join(" ",
                    executable,
                    "-c " + cOption,
                    "-d " + dOption,
                    "-e " + eOption,
                    "-s " + sOptions,
                    "-p " + pOptions);
            Runtime run  = Runtime.getRuntime();
            System.out.println(command);
            Process proc = run.exec(command);
            proc.waitFor();
            System.out.println(proc.exitValue());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private String getAlphaNumericString(int n)
    {

        // chose a Character random from this String 
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString 
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between 
            // 0 to AlphaNumericString variable length 
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb 
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }


}
