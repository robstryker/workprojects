package net.oxbeef.jbt.plugins.dependencies.ui.dialogs;

import java.io.File;
import java.util.ArrayList;

import net.oxbeef.jbt.plugins.dependencies.Activator;
import net.oxbeef.jbt.plugins.dependencies.core.IDependencyModel;
import net.oxbeef.jbt.plugins.dependencies.core.IModelLoader;
import net.oxbeef.jbt.plugins.dependencies.core.ModelProviders;
import net.oxbeef.jbt.plugins.dependencies.ui.IModelUI;
import net.oxbeef.jbt.plugins.dependencies.ui.ModelUIProviders;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoadGraphDialog extends TitleAreaDialog {
	
	private Combo strategyCombo, componentNameCombo;
	private Text rootText;
	private Button loadComponents;
	String compNameVal;
	String graphTypeVal;
	String root, strategyVal;
	Combo graphTypeCombo;
	public LoadGraphDialog(Shell parentShell) {
		super(parentShell);
	}
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite)super.createDialogArea(parent);
		Composite main = new Composite(c, SWT.BORDER);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new FormLayout());
		

		Label strat = new Label(main, SWT.NONE);
		strategyCombo = new Combo(main, SWT.SINGLE|SWT.BORDER);
		strategyCombo.setItems(ModelProviders.getLoaderIds());
		strategyCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String str = strategyCombo.getText();
				IModelUI ui = ModelUIProviders.getUI(str);
				if( ui != null ) {
					String[] types = ui.getGraphTypes();
					if( types != null ) {
						graphTypeCombo.setItems(types);
					}
				}
			}
		});
		
		
		
		ModifyListener m1 = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				storeValues();
			}
		};
		
		Label root = new Label(main, SWT.NONE);
		root.setText("Repos root: ");
		rootText = new Text(main, SWT.SINGLE | SWT.BORDER);
		rootText.setText(Activator.getDefault().getRepositoryRoot().getAbsolutePath());
		rootText.addModifyListener(m1);
		
		
		Label graphType = new Label(main, SWT.NONE);
		graphType.setText("Graph Type: ");
		graphTypeCombo = new Combo(main, SWT.READ_ONLY | SWT.BORDER);
		
		graphTypeCombo.setItems(new String[] { });
		graphTypeCombo.addModifyListener(m1);
		
		
		Label compNameLabel = new Label(main, SWT.NONE);
		compNameLabel.setText("Component name: ");
		loadComponents = new Button(main, SWT.PUSH);
		loadComponents.setText("Load...");
		componentNameCombo = new Combo(main, SWT.SINGLE | SWT.BORDER);
		loadComponents.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String strat = strategyVal;
				if( strat != null ) {
					IModelLoader ml = ModelProviders.getLoader(strat);
					if( ml != null ) {
						File homedir = new File(rootText.getText());
						if( homedir.exists()) {
							IDependencyModel depModel = ml.loadModel(homedir, null, null);
							if( depModel != null ) {
								String[] allComps = depModel.getComponents();
								componentNameCombo.setItems(allComps);
							}
						}
					}
				}
			}
		});
		
		
		

		componentNameCombo.addModifyListener(m1);
		String[] components = getComponents();
		componentNameCombo.setItems(components);
		
		
		storeValues();
		
		strat.setLayoutData(createFormData(0,5,null,0,0,5,null,0));
		strategyCombo.setLayoutData(createFormData(0,5,null,0,strat,5,100,-5));
		
		root.setLayoutData(createFormData(strategyCombo,5,null,0,0,5,null,0));
		rootText.setLayoutData(createFormData(strategyCombo,5,null,0,root,5,100,-5));
		
		graphType.setLayoutData(createFormData(rootText, 5, null, 0, 0, 5, null, 0));
		graphTypeCombo.setLayoutData(createFormData(rootText, 5, null, 0, graphType, 5, 100, -5));
		
		compNameLabel.setLayoutData(createFormData(graphTypeCombo,5,null,0,0,5,null,0));
		loadComponents.setLayoutData(createFormData(graphTypeCombo,5,null,0,75,5,100,-5));
		componentNameCombo.setLayoutData(createFormData(graphTypeCombo,5,null,0,compNameLabel,5,loadComponents,-5));
		return c;
	}
	
	private String[] getComponents() {
		ArrayList<String> comps = new ArrayList<String>();
		File f = Activator.getRepositoryRoot();
		File[] children = f.listFiles();
		for( int i = 0; i < children.length; i++ ) {
			if( children[i].isDirectory() && new File(children[i], "plugins").exists())
				comps.add(children[i].getName());
			else if(children[i].isDirectory() & children[i].isDirectory()) {
				File[] grandChildren = children[i].listFiles();
				for( int j = 0; j < grandChildren.length; j++ ) {
					if( grandChildren[j].isDirectory() && new File(grandChildren[j], "plugins").exists()) {
						comps.add(grandChildren[j].getName());
					}
				}
			}
		}
		return (String[]) comps.toArray(new String[comps.size()]);
	}
	
	public void storeValues() {
		root = rootText.getText();
		compNameVal = componentNameCombo.getText();
		strategyVal = strategyCombo.getText();
		graphTypeVal = graphTypeCombo==null?"":graphTypeCombo.getText();
	}
	
	public String getGraphType() {
		return graphTypeVal;
	}
	public String getComponentName() {
		return compNameVal;
	}
	public String getRootDir() {
		return root;
	}
	public String getStrategy() {
		return strategyVal;
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
