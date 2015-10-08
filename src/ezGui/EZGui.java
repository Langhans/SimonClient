package ezGui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.Locale;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import pTimEvent.IEventHandler;
import pTimEvent.TimEvent;
import pTimEvent.TimEvent.Tag;
import pTimEvent.TimEventBus;
import static pTimEvent.TimEvent.*;

import static pSimonGenerell.Constants.*;


public class EZGui extends JFrame implements ActionListener , IEventHandler{

    private final JPanel mainpanel;
    private final JButton getFile_Button;
    private final JTabbedPane infoPane;
    
    private final JButton startRunner_but;
    private final JButton listFilesButton;
    private final JList list;
    private final JPanel buttonPanel;
    private final JScrollPane scrPane;
    private final JTextArea error_area;
    private final JButton error_but1;
    private final JButton error_but2;
    private final JScrollPane error_scrPane;
    private final JPanel errorPanel;
    
    private final TimEventBus eventbus = TimEventBus.INSTANCE;
    
    
    public EZGui(){
      
        // init all panels
        mainpanel = new JPanel();
        infoPane = new JTabbedPane();
        errorPanel = new JPanel();
        buttonPanel = new JPanel();
        
        // build errorPanel  
        error_area = new JTextArea("" , 25 , 50 );
        error_but1 = new JButton("CLEAR LOG");
        error_but2 = new JButton("SAVE LOG");
        error_scrPane = new JScrollPane();
        error_scrPane.getViewport().add(error_area, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        errorPanel.add(error_scrPane);
        errorPanel.add(error_but1);
        infoPane.addTab("LOG", errorPanel);
        
        // build ButtonPanel
        getFile_Button = new JButton("GET");
        startRunner_but = new JButton("START");
        startRunner_but.addActionListener(this);
        getFile_Button.addActionListener(this);
        listFilesButton = new JButton("LIST_FILES");
        listFilesButton.addActionListener(this);
        
        // build filelist
        list = new JList(new String[]{"no files to show" } );
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setMinimumSize(new Dimension ( 350  , 350 ));
        scrPane = new JScrollPane();
        scrPane.getViewport().add(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        infoPane.addTab("FILES", scrPane);

        
       // lay out buttonPanel
        buttonPanel.add(getFile_Button );
        buttonPanel.add(startRunner_but );
        buttonPanel.add(listFilesButton  );
        
        // lay out mainPanel
        BorderLayout layout = new BorderLayout();
        mainpanel.setLayout(layout);
        mainpanel.add(buttonPanel , BorderLayout.NORTH);
        mainpanel.add( infoPane , BorderLayout.CENTER);
        this.add(mainpanel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        
        // register Gui to eventbus for TimEvents
        eventbus.registerHandler(this);
        
        WindowAdapter wadapt = new WindowAdapter(){
          
          @Override
          public void windowClosing( WindowEvent e){
            eventbus.publish(new TimEvent( TimEvent.Tag.CLIENT_SHUTDOWN , null));
            
          }
        };
        this.addWindowListener(wadapt);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      
        if (e.getSource() == getFile_Button && !list.isSelectionEmpty() ){
           eventbus.publish( new TimEvent( Tag.SENDFILE , 
                   list.getSelectedValue() ) );
           eventbus.publish(new TimEvent( Tag.INFO , FILE_REQUESTED + (String)list.getSelectedValue() ));
           this.revalidate();
           this.repaint();
           
        } else if (e.getSource() == startRunner_but ){
          eventbus.publish(new TimEvent( Tag.INFO , RUNNER_SEND ));
            eventbus.publish(new TimEvent(Tag.SENDRUNNER , null ));
            
        } else if (e.getSource() == listFilesButton ){
            eventbus.publish(new TimEvent(Tag.FILELIST , null));
        }
        else{
        	System.out.println("GUI SEND UNKNOWN ACTIONEVENT");
        }
    }
    
    private void addInfo( String message ){
        error_area.append( "Received at " + new Date() + ": "+ message +"\n"   );
    }
    
        

	@Override
	public void onEvent(TimEvent evt) {
		// TODO Auto-generated method stub
		switch( evt.getTag() ){
		
		case GUIFILELIST:
			list.setListData( (String[]) evt.getObject() ); 
			this.repaint();
			break;
			
		case INFO:
		  addInfo((String)evt.getObject());
		break;
		}
	}  
}
