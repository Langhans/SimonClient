package pTimClient;

import java.awt.Dimension;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import pSimonGenerell.IClientCallBack;
import pSimonGenerell.ITimRunner;
import pSimonGenerell.ITimServer;
import pTimEvent.IEventHandler;
import pTimEvent.TimEvent;
import pTimEvent.TimEvent.Tag;
import pTimEvent.TimEventBus;
import de.root1.simon.Lookup;
import de.root1.simon.Simon;
import de.root1.simon.annotation.*;
import de.root1.simon.exceptions.SimonRemoteException;
import ezGui.EZGui;
import ezGui.PicResizePanel;
import static pSimonGenerell.Constants.*;

@SimonRemote(value = { IClientCallBack.class })
public class TimClient implements IEventHandler, IClientCallBack {

  private final String host;
  private final int reg_port;
  private ITimServer server;

  private Lookup lookup;
  private boolean connected = false;

  private final EZGui gui = new EZGui();
  private final TimEventBus eventbus = TimEventBus.INSTANCE;

  public TimClient(String host, int reg_port) {

    this.host = host;
    this.reg_port = reg_port;
    connect();
    eventbus.registerHandler(this);
  }

  
  public void connect() {

    try {
      lookup = Simon.createNameLookup(this.host, reg_port);
      Object obj = lookup.lookup("TimServer");
      server = (ITimServer) obj;
      server.login(this);
      System.out.println("Client connected to and registered of server!");
      
    } catch (Exception e) {
      e.printStackTrace();
      eventbus.publish(
          new TimEvent(Tag.INFO, "COULD NOT CONNECT TO SERVER " + this.host));
      int answer = JOptionPane.showConfirmDialog(null, ASK_FOR_RECONNECTING);
      
      if (answer == JOptionPane.OK_OPTION) {
        connect();
      } else {
        System.exit(1);
      }
    }
    connected = true;
  }

  /**
   * Tries to download the specified file from absolute path to file (input
   * parameter). Tries to read the file as an Image! Publishes to the event bus
   * if exception occurs and might return null!
   * 
   * @returns requested Image. Returns null in case of Exception!
   */
  public Image getFile(String name) {
    if (!connected) {
      eventbus.publish(new TimEvent(Tag.INFO, ASK_FOR_RECONNECTING));
      return null;
    } else {

      final File file; // a temp file 
      final byte[] file_bytes;
      try {
        file_bytes = server.serveFile(name);
        file = File.createTempFile(name, null); // "name.tmp"

        FileOutputStream fOut = new FileOutputStream(file);
        fOut.write(file_bytes);
        fOut.close();
        
      } catch (Exception e) {
        e.printStackTrace();
        eventbus.publish(new TimEvent(Tag.INFO, "Cannot receive file"));
        return null;
      }
      
        Image img = null;
        try{
        img = null;
        img = ImageIO.read(file);
        } catch (IOException e){
          ; // just return the img = null!
        }
        
        if (img != null ){
          return img;
        } else{
        // Ask user for save file!
        JFileChooser save_choose = new JFileChooser(
            new File(System.getProperty("user.dir")));

        save_choose.setDialogTitle("Save as...");
        int choice = save_choose.showSaveDialog(null);
        if (choice == JFileChooser.APPROVE_OPTION) {
          FileOutputStream fOut = null;
          try {
            fOut = new FileOutputStream(save_choose.getSelectedFile());
            fOut.write(file_bytes);
            fOut.flush();
            fOut.close();
            eventbus.publish(new TimEvent(Tag.INFO,
                "File " + name + " downloaded and saved to "
                    + save_choose.getSelectedFile().getName()));
            return null;
          } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            eventbus.publish(new TimEvent(Tag.INFO,
                "File " + name + " downloaded but could not be saved!"));
            return null;

          }
        } else {
          eventbus.publish(new TimEvent(Tag.INFO,
              "File " + name + " downloaded but not saved "));
          return null;
        }
      }
    }
  }

  public void submitRunnable(Runnable r) {
    try {
      server.submitRunnable(r);
    } catch (SimonRemoteException e) {
      e.printStackTrace();
      eventbus.publish(new TimEvent(Tag.INFO, "Cannot send runnable"));
    }
  }

  public void shutdown() {
    connected = false;
    System.out.println("Good bye!");
    if (lookup != null) {
      lookup.release(server);
    }
    System.exit(0);
  }

  @Override
  public void callback(String message) throws SimonRemoteException {
    JOptionPane.showMessageDialog(null, message);
    eventbus.publish(new TimEvent(Tag.INFO, message));
  }

  @Override
  public void ping() throws SimonRemoteException {
    // just do nothing! Server wants to check connection!
  }

  @Override
  public void onEvent(TimEvent evt) {

    switch (evt.getTag()) {

      case SENDFILE:
        Image img = this.getFile((String) evt.getObject());
        System.out.println("Image downloaded: " + img);
        // FIXME easy and dumm check for nullpointerException
        if (img != null) {
          showPreviewOfPicture(img);
        }
        break;

      case SENDRUNNER:
        try {
          Lookup lookup = Simon.createNameLookup(SERVER_HOST, REG_PORT);
          ITimRunner runner = (ITimRunner) lookup.lookup("TimRunner");
          server.submitRunnable(runner);
        } catch (Exception e) {
          e.printStackTrace();
        }
        break;
        
      case FILELIST:
        String[] filelist = server.listFiles();
        eventbus.publish(new TimEvent(Tag.GUIFILELIST, filelist));
        break;
        
      case INFO:
        System.out.println("Client received INFO-TAG!\n" + evt.getObject());
        break;
        
      case CLIENT_SHUTDOWN:
        this.shutdown();

      default:
        System.out.println("Client received unknown Tag on Event-Bus!");
    }
  }

  
  private static void showPreviewOfPicture( Image img ){
    PicResizePanel panel = new PicResizePanel(img);
    JFrame frame = new JFrame("PREVIEW");
    frame.add(panel);
    frame.setMinimumSize(new Dimension(300, 300));
    frame.pack();
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }
  
  
  public static void main(String[] args) {
    // TODO Auto-generated method stub

    if (args.length > 0) {
      try {
        System.out.println("Client started with cmd-arguments!");
        int port = Integer.parseInt(args[1]);
        new TimClient(args[0], port);
      } catch (Exception e) {
        System.out.println(
            "Could not read arguments! Please restart the application!");
      }
    } else
      new TimClient(SERVER_HOST, REG_PORT);
  }
}
