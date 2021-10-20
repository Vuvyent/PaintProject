package view;

import model.*;
import java.awt.*;
import javax.swing.*;

public class MainMenu  extends JPanel{
    private JButton create;
    private JButton open;
    private JTextField createText, openText;

    public MainMenu(){
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.bottom = 50;
        create = new JButton("Create");
        //create.setFont(new Font("Arial", Font.PLAIN, 24));
        create.setPreferredSize(new Dimension(100, 20));
        createText = new JTextField(20);
        open = new JButton("Open");
        openText = new JTextField(20);
        //open.setFont(new Font("Arial", Font.PLAIN, 24));
        open.setPreferredSize(new Dimension(100, 20));
        add(createText, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(create, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(openText, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(open, gbc);
    }

    public void addListener(Client listener){
        create.addActionListener(listener);
        open.addActionListener(listener);
    }

    public JButton getOpen(){
        return open;
    }

    public JButton getCreate(){
        return create;
    }

    public JTextField getCreateText(){
        return createText;
    }

    public JTextField getOpenText(){
        return openText;
    }
}

/*public class MainMenu  extends JComponent{
    private JPanel menuPanel;
    private JButton create;
    private JButton open;

    public MainMenu(){
        menuPanel = new JPanel();
        menuPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        create = new JButton("Create new paint panel");
        open = new JButton("Open existing paint panel");
        menuPanel.add(create, gbc);
        menuPanel.add(open, gbc);
        add(menuPanel);
    }
}*/
