package net.oxbeef.jbt.plugins.dependencies.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoadGraphDialog extends TitleAreaDialog {
	private Text componentNameText;
	String compNameVal;
	public LoadGraphDialog(Shell parentShell) {
		super(parentShell);
	}
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite)super.createDialogArea(parent);
		Composite main = new Composite(c, SWT.BORDER);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new FormLayout());
		Label l = new Label(main, SWT.NONE);
		l.setText("Component name: ");
		componentNameText = new Text(main, SWT.SINGLE | SWT.BORDER);
		
		l.setLayoutData(createFormData(0,5,null,0,0,5,null,0));
		componentNameText.setLayoutData(createFormData(0,5,null,0,l,5,null,0));
		componentNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				compNameVal = componentNameText.getText();
			}
		});
		return c;
	}
	
	public String getComponentName() {
		return compNameVal;
	}
	
	public static FormData createFormData(Object topStart, int topOffset, Object bottomStart, int bottomOffset, 
			Object leftStart, int leftOffset, Object rightStart, int rightOffset) {
		FormData data = new FormData();

		if( topStart != null ) {
			data.top = topStart instanceof Control ? new FormAttachment((Control)topStart, topOffset) : 
				new FormAttachment(((Integer)topStart).intValue(), topOffset);
		}

		if( bottomStart != null ) {
			data.bottom = bottomStart instanceof Control ? new FormAttachment((Control)bottomStart, bottomOffset) : 
				new FormAttachment(((Integer)bottomStart).intValue(), bottomOffset);
		}

		if( leftStart != null ) {
			data.left = leftStart instanceof Control ? new FormAttachment((Control)leftStart, leftOffset) : 
				new FormAttachment(((Integer)leftStart).intValue(), leftOffset);
		}

		if( rightStart != null ) {
			data.right = rightStart instanceof Control ? new FormAttachment((Control)rightStart, rightOffset) : 
				new FormAttachment(((Integer)rightStart).intValue(), rightOffset);
		}
		return data;
	}

}
