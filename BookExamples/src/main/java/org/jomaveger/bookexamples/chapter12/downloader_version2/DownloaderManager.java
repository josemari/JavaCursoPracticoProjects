package org.jomaveger.bookexamples.chapter12.downloader_version2;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.*;

public class DownloaderManager extends JPanel {

    private final Downloader downloader;

    private JButton startButton;
    private JButton sleepButton;
    private JButton suspendButton;
    private JButton resumeButton;
    private JButton stopButton;

    public DownloaderManager(URL source, OutputStream os)
            throws IOException {
        downloader = new Downloader(source, os);
        buildLayout();
        Border border = new BevelBorder(BevelBorder.RAISED);
        String name = source.toString();
        int index = name.lastIndexOf('/');
        border = new TitledBorder(border,
                name.substring(index + 1));
        setBorder(border);
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        downloader.setBorder(new BevelBorder(BevelBorder.RAISED));
        add(downloader, BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel getButtonPanel() {
        JPanel outerPanel;
        JPanel innerPanel;

        innerPanel = new JPanel();
        innerPanel.setLayout(new GridLayout(1, 5, 10, 0));

        startButton = new JButton("Start");
        startButton.addActionListener((ActionEvent event) -> {
            startButton.setEnabled(false);
            sleepButton.setEnabled(true);
            resumeButton.setEnabled(false);
            suspendButton.setEnabled(true);
            stopButton.setEnabled(true);
            downloader.startDownload();
        });
        innerPanel.add(startButton);

        sleepButton = new JButton("Sleep");
        sleepButton.setEnabled(false);
        sleepButton.addActionListener((ActionEvent event) -> {
            downloader.setSleepScheduled(true);
        });
        innerPanel.add(sleepButton);

        suspendButton = new JButton("Suspend");
        suspendButton.setEnabled(false);
        suspendButton.addActionListener((ActionEvent event) -> {
            suspendButton.setEnabled(false);
            resumeButton.setEnabled(true);
            stopButton.setEnabled(true);
            downloader.setSuspended(true);
        });
        innerPanel.add(suspendButton);

        resumeButton = new JButton("Resume");
        resumeButton.setEnabled(false);
        resumeButton.addActionListener((ActionEvent event) -> {
            resumeButton.setEnabled(false);
            suspendButton.setEnabled(true);
            stopButton.setEnabled(true);
            downloader.resumeDownload();
        });
        innerPanel.add(resumeButton);

        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        stopButton.addActionListener((ActionEvent event) -> {
            stopButton.setEnabled(false);
            sleepButton.setEnabled(false);
            suspendButton.setEnabled(false);
            resumeButton.setEnabled(false);
            downloader.stopDownload();
        });
        innerPanel.add(stopButton);

        outerPanel = new JPanel();
        outerPanel.add(innerPanel);
        return outerPanel;
    }
}
