import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import java.util.*;
import hec.dssgui.ListSelection;
import javax.swing.JMenuItem;

public class HelloPlugin
{
    public static void main(Object[] args)
    {
        final HelloPlugin plugin = new HelloPlugin();
        final ListSelection listSelection = (ListSelection)args[0];
        JMenuItem genericMenuItem = new JMenuItem("Hello Plugin");
        genericMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                plugin.process(listSelection);
            }
        });
        listSelection.registerPlugin(ListSelection.TOOLS, genericMenuItem);
    }
    protected void process(ListSelection listSelection)
    {
       JOptionPane.showMessageDialog(listSelection, "Hello from Your plugin",
                    "Java Plugin", JOptionPane.OK_OPTION);
            return;
    }

}