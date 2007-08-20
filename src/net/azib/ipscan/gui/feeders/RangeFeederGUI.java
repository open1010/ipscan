/**
 * This file is a part of Angry IP Scanner source code,
 * see http://www.azib.net/ for more information.
 * Licensed under GPLv2.
 */
package net.azib.ipscan.gui.feeders;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import net.azib.ipscan.config.Labels;
import net.azib.ipscan.config.LoggerFactory;
import net.azib.ipscan.config.Platform;
import net.azib.ipscan.core.InetAddressUtils;
import net.azib.ipscan.feeders.Feeder;
import net.azib.ipscan.feeders.FeederException;
import net.azib.ipscan.feeders.RangeFeeder;
import net.azib.ipscan.gui.actions.FeederActions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * GUI for initialization of RangeFeeder.
 * 
 * @author Anton Keks
 */
public class RangeFeederGUI extends AbstractFeederGUI {
	
	static final Logger LOG = LoggerFactory.getLogger();

	private Label ipRangeLabel;
	private Text startIPText;
	
	private Label toLabel;
	private Text endIPText;
	private boolean isEndIPUnedited = true;
		
	private Label hostnameLabel;
	private Text hostnameText;
	
	private Button ipUpButton;
	
	private Combo netmaskCombo;
	
	public RangeFeederGUI(Composite parent) {
		super(parent);
	}

	protected void initialize() {
		feeder = new RangeFeeder();
		
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = 3;
		formLayout.marginHeight = 3;
		formLayout.marginBottom = 2;
		formLayout.spacing = 4;
		setLayout(formLayout);
		
        ipRangeLabel = new Label(this, SWT.NONE);
        startIPText = new Text(this, SWT.BORDER);
        toLabel = new Label(this, SWT.NONE);
        endIPText = new Text(this, SWT.BORDER);
        hostnameLabel = new Label(this, SWT.NONE);
        hostnameText = new Text(this, SWT.BORDER);
		ipUpButton = new Button(this, SWT.NONE);
        netmaskCombo = new Combo(this, SWT.NONE);
        
        ipRangeLabel.setText(getStringLabel("startIP"));
        FormData formData = new FormData();
		formData.right = new FormAttachment(hostnameLabel, 0, SWT.RIGHT);
        formData.top = new FormAttachment(startIPText, 0, SWT.CENTER);
        ipRangeLabel.setLayoutData(formData);
        
		formData = new FormData(105 + (Platform.MAC_OS ? 35 : 0), SWT.DEFAULT);
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(ipRangeLabel);
        startIPText.setLayoutData(formData);
        startIPText.addModifyListener(new StartIPModifyListener());
        
        toLabel.setText(getStringLabel("endIP"));
        formData = new FormData();
        formData.left = new FormAttachment(startIPText);
        formData.top = new FormAttachment(startIPText, 0, SWT.CENTER);
        toLabel.setLayoutData(formData);
                
		formData = new FormData(105 + (Platform.MAC_OS ? 35 : 0), SWT.DEFAULT);
		formData.left = new FormAttachment(toLabel);
        endIPText.setLayoutData(formData);
        endIPText.addKeyListener(new EndIPKeyListener());
        
        FeederActions.HostnameButton hostnameListener = new FeederActions.HostnameButton(hostnameText, startIPText) {
			public void widgetSelected(SelectionEvent event) {
				// raise the flag
				isEndIPUnedited = true;
				// reset the netmask combo
				netmaskCombo.setText(getStringLabel("netmask"));
				// now do the stuff
				super.widgetSelected(event);
			}
        };
        
        hostnameText.addTraverseListener(hostnameListener);
        formData = new FormData(105, SWT.DEFAULT);
		formData.top = new FormAttachment(startIPText);
		formData.left = new FormAttachment(startIPText, 0, SWT.LEFT);
		hostnameText.setLayoutData(formData);
		hostnameText.setToolTipText(Labels.getLabel("feeder.range.hostname.tooltip"));
        
        hostnameLabel.setText(getStringLabel("hostname"));
        formData = new FormData();
        formData.left = new FormAttachment(0);
		formData.top = new FormAttachment(hostnameText, 0, SWT.CENTER);
		hostnameLabel.setLayoutData(formData);
		
		ipUpButton.setImage(new Image(getDisplay(), Labels.getInstance().getImageAsStream("button.ipUp.img")));
		ipUpButton.setText(Labels.getLabel("button.ipUp"));
		ipUpButton.addSelectionListener(hostnameListener);
		formData = new FormData();
		formData.top = new FormAttachment(endIPText);
		formData.left = new FormAttachment(hostnameText);
		formData.bottom = new FormAttachment(hostnameText, 1, SWT.BOTTOM);
		ipUpButton.setLayoutData(formData);
        
        netmaskCombo.setText(getStringLabel("netmask"));
		netmaskCombo.setVisibleItemCount(10);
		netmaskCombo.add("/16");
		netmaskCombo.add("/24");
		netmaskCombo.add("/28");
		// Warning: IPv4 specific netmasks
		netmaskCombo.add("255...192");
		netmaskCombo.add("255...128");
		netmaskCombo.add("255...0");
		netmaskCombo.add("255..0.0");
		netmaskCombo.add("255.0.0.0");
		NetmaskListener netmaskSelectionListener = new NetmaskListener();
		netmaskCombo.addListener(SWT.Selection, netmaskSelectionListener);
		netmaskCombo.addListener(SWT.Traverse, netmaskSelectionListener);
		formData = new FormData();
		formData.top = new FormAttachment(startIPText);
		formData.left = new FormAttachment(ipUpButton, 5);
		formData.right = new FormAttachment(endIPText, 0, SWT.RIGHT);
		formData.bottom = new FormAttachment(hostnameText, 0, SWT.BOTTOM);
		netmaskCombo.setLayoutData(formData);
		netmaskCombo.setToolTipText(Labels.getLabel("feeder.range.netmask.tooltip"));

		// do this stuff asynchronously (to show GUI faster)
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				// fill the IP and hostname fields with local hostname and IP addresses
				try {
					hostnameText.setText(InetAddress.getLocalHost().getHostName());
					startIPText.setText(InetAddressUtils.getAddressByName(hostnameText.getText()));
					endIPText.setText(startIPText.getText());
				}
				catch (UnknownHostException e) {
					// don't report any errors on initialization, leave fields empty
					LOG.fine(e.toString());
				}
			}
		});
                
		pack();
	}

	public Feeder getFeeder() {
		((RangeFeeder)feeder).initialize(startIPText.getText(), endIPText.getText());
		return feeder;
	}
	
	public String serialize() {
		return startIPText.getText() + ":::" + endIPText.getText();
	}

	public void unserialize(String serialized) {
		String[] parts = serialized.split(":::");
		startIPText.setText(parts[0]);
		endIPText.setText(parts[1]);
		// reset the netmask combo
		netmaskCombo.setText(getStringLabel("netmask"));
	}

	final class EndIPKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e) {
			isEndIPUnedited = false;
		}

		public void keyReleased(KeyEvent e) {
		}
	}

	final class StartIPModifyListener implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			if (isEndIPUnedited) {
				endIPText.setText(startIPText.getText());
			}
		}
	}

	final class NetmaskListener implements Listener {
		public void handleEvent(Event event) {
			if (event.type == SWT.Traverse) {
				// skip any other traversal besides RETURN
				if (event.detail != SWT.TRAVERSE_RETURN) 
					return;
				event.doit = false;
			}
			if (event.type == SWT.Selection) {
				// this is a workaround for a strange bug: if text is just typed in the combo,
				// then this event is sent after each keypress, but we want it to be fired
				// only if something is selected from the drop down
				if (netmaskCombo.indexOf(netmaskCombo.getText()) < 0)
					return;
			}
			
			try {
				String netmaskString = netmaskCombo.getText();
				InetAddress netmask = InetAddressUtils.parseNetmask(netmaskString);
				InetAddress startIP = InetAddress.getByName(startIPText.getText());
				
				startIPText.setText(InetAddressUtils.startRangeByNetmask(startIP, netmask).getHostAddress());
				endIPText.setText(InetAddressUtils.endRangeByNetmask(startIP, netmask).getHostAddress());
				isEndIPUnedited = false;
				
				netmaskCombo.forceFocus();
			}
			catch (UnknownHostException e) {
				throw new FeederException("invalidNetmask");
			}
		}
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"