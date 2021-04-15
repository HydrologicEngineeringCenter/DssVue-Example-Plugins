import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.*;
import java.io.*;

import hec.dssgui.*;
import hec.heclib.dss.DSSPathname;
import hec.script.MessageBox;
import rma.swing.text.DssPathnamePartDocument;

class StreamGobbler extends Thread // from: https://www.infoworld.com/article/2071275/when-runtime-exec---won-t.html
{
    InputStream is;
    String type;

    StreamGobbler(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
    }

    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
                System.out.println(type + ">" + line);
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}

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
            public void actionPerformed(ActionEvent e) { plugin.importFromExcel(listSelection); }
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
        String oldDir = System.getProperty("user.dir");
        try
        {
            // Set new current working directory
            String DSSExcelDir = oldDir + "\\dotnet\\DSSExcelImport\\";
            System.setProperty("user.dir", DSSExcelDir);

            // Get Excel file
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Excel Files", "xls", "xlsx");
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setFileFilter(filter);
            int result = fileChooser.showOpenDialog(null);
            if (result != JFileChooser.APPROVE_OPTION)
                return;

            // Create command line argument string for DSSExcel
            String executable = System.getProperty("user.dir") + "DSSExcelCLI.exe";
            String cOption = "import";
            String dOption = listSelection.getDSSFilename();
            String eOption = fileChooser.getSelectedFile().getAbsolutePath();
            String command = String.join(" ",
                    executable,
                    "-c " + cOption,
                    "-d " + dOption,
                    "-e " + eOption);

            Runtime run  = Runtime.getRuntime();
            System.out.println(command);
            Process proc = run.exec(command);

            // setting up proper error stream for import process
            StreamGobbler errorGobbler = new
                    StreamGobbler(proc.getErrorStream(), "ERROR");

            // setting up proper output stream for import process
            StreamGobbler outputGobbler = new
                    StreamGobbler(proc.getInputStream(), "OUTPUT");

            // start streams
            errorGobbler.start();
            outputGobbler.start();

            proc.waitFor();

            // change current directory to old directory
            System.setProperty("user.dir", oldDir);

            // refresh DSS file in DSSVue
            listSelection.closeDssFile();
            listSelection.openDSSFile(dOption);

            // Display Import Status
            if (proc.exitValue() == 0)
                MessageBox.showInformation("Import succeeded.", "Import Status");
            else
                MessageBox.showError("Import failed.", "Import Status");
        }
        catch (Exception e)
        {
            System.setProperty("user.dir", oldDir);
            e.printStackTrace();
        }
    }

    private void exportToExcel(ListSelection listSelection)
    {
        String oldDir = System.getProperty("user.dir");
        try
        {
            // Check if records have been selected in DSSVue
            if (listSelection.getNumberSelectedPathnames() == 0)
            {
                MessageBox.showError("Select DSS records to export.", "Export Error");
                return;
            }

            // Set new current working directory
            String DSSExcelDir = oldDir + "\\dotnet\\DSSExcelExport\\";
            System.setProperty("user.dir", DSSExcelDir);

            // Get Excel file
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Excel Files", "xls", "xlsx");
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setFileFilter(filter);
            int result = fileChooser.showOpenDialog(null);
            if (result != JFileChooser.APPROVE_OPTION)
                return;

            // Create command line argument string for DSSExcel
            DataReferenceSet v = listSelection.getSelectedPathnames();
            String executable = System.getProperty("user.dir") + "DSSExcelCLI.exe";
            String cOption = "export";
            String dOption = listSelection.getDSSFilename();
            String eOption = fileChooser.getSelectedFile().getAbsolutePath();
            List<String> p = new ArrayList<>();
            for (int i = 0; i < v.size(); i++) // Get selected paths
            {
                DSSPathname path = new DSSPathname();
                path.setPathname(v.elementAt(i).pathname());
                p.add("/" + path.aPart() + "/" + path.bPart() + "/" + path.cPart() + "//" + path.ePart() + "/" + path.fPart() + "/");
            }
            String pOptions = String.join(";", p);
            pOptions = "\"" + pOptions + "\"";
            String command = String.join(" ",
                    executable,
                    "-c " + cOption,
                    "-d " + dOption,
                    "-e " + eOption,
                    "-p " + pOptions);
            Runtime run  = Runtime.getRuntime();
            System.out.println(command);
            Process proc = run.exec(command);
            proc.waitFor();

            // change current directory to old directory
            System.setProperty("user.dir", oldDir);

            // Display Export Status
            if (proc.exitValue() == 0)
                MessageBox.showInformation("Export succeeded.", "Export Status");
            else
                MessageBox.showError("Export failed.", "Export Status");
        }
        catch (Exception e)
        {
            System.setProperty("user.dir", oldDir);
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
