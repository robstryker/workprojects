package net.oxbeef.jbt.plugins.dependencies.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoadGraphDialog extends TitleAreaDialog {
	public static final String FULL_GRAPH = "Full Product";
	public static final String ONE_COMP = "One Component's Dependencies";
	public static final String ONE_COMP_EXTERNALS = "One Component's Dependencies (with externals)";
	public static final String OTHER_COMPONENTS_AGAINST_THIS = "Other components depending on this component";
	public static final String OTHER_COMPONENTS_AGAINST_THIS_EXTERNALS = "Other components depending on this component (with externals)";
	
	private Combo componentNameCombo;
	String compNameVal;
	String graphTypeVal;
	Combo graphTypeCombo;
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
		componentNameCombo = new Combo(main, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		
		l.setLayoutData(createFormData(0,5,null,0,0,5,null,0));
		componentNameCombo.setLayoutData(createFormData(0,5,null,0,l,5,100,-5));
		ModifyListener m1 = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				storeValues();
			}
		};
		componentNameCombo.addModifyListener(m1);
		
		Label graphType = new Label(main, SWT.NONE);
		graphType.setText("Graph Type: ");
		graphType.setLayoutData(createFormData(componentNameCombo, 5, null, 0, 0, 5, null, 0));
		graphTypeCombo = new Combo(main, SWT.READ_ONLY | SWT.BORDER);
		
		graphTypeCombo.setItems(new String[] { 
				FULL_GRAPH, ONE_COMP, ONE_COMP_EXTERNALS,
				OTHER_COMPONENTS_AGAINST_THIS,
				OTHER_COMPONENTS_AGAINST_THIS_EXTERNALS
		} );
		
		graphTypeCombo.setLayoutData(createFormData(componentNameCombo, 5, null, 0, graphType, 5, 100, -5));
		graphTypeCombo.addModifyListener(m1);
		storeValues();
		return c;
	}
	public void storeValues() {
		compNameVal = componentNameCombo.getText();
		graphTypeVal = graphTypeCombo.getText();
	}
	
	public String getGraphType() {
		return graphTypeVal;
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
