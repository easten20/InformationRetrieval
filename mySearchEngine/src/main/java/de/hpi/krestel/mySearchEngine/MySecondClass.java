package de.hpi.krestel.mySearchEngine;

import java.awt.*;

import javax.swing.*;

import javax.swing.border.*;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class MySecondClass extends JFrame
{
    private JPanel topPanel;
    private JTextPane tPane;
    private JScrollPane sp;

    public MySecondClass()
    {
    	setExtendedState( this.getExtendedState()|JFrame.MAXIMIZED_BOTH );
        topPanel = new JPanel();     

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);            

        EmptyBorder eb = new EmptyBorder(new Insets(10, 10, 10, 10));

        tPane = new JTextPane();  
        tPane.setBorder(eb);
        //tPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        tPane.setMargin(new Insets(5, 5, 5, 5));

        topPanel.add(tPane);

        getContentPane().add(topPanel);
        
        sp = new JScrollPane(tPane);
        getContentPane().add( sp );

        pack();
        setVisible(true);   
    }

    public void appendToPane(String msg, Color c)
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        int len = tPane.getDocument().getLength();
        tPane.setCaretPosition(len);
        tPane.setCharacterAttributes(aset, false);
        tPane.replaceSelection(msg);
    }

}