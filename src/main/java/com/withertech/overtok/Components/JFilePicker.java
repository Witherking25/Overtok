package com.withertech.overtok.Components;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class JFilePicker
{
    private JTextField pathTextField;
    private JButton pathChooserButton;
    private JPanel JFilePicker;
    public File outputDirectory;

    public JFilePicker()
    {
        pathChooserButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int option = fileChooser.showOpenDialog(JFilePicker);
                if (option == JFileChooser.APPROVE_OPTION)
                {
                    outputDirectory = fileChooser.getSelectedFile();
                    pathTextField.setText(outputDirectory.getPath());
                }
            }
        });
    }
}
