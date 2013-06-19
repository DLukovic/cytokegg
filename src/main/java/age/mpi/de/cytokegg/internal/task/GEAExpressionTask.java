package age.mpi.de.cytokegg.internal.task;

import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import age.mpi.de.cytokegg.internal.service.GEADAS;
import age.mpi.de.cytokegg.internal.service.GeaApi;
import age.mpi.de.cytokegg.internal.service.KeggService;
import age.mpi.de.cytokegg.internal.ui.GEASummaryPanel;
import age.mpi.de.cytokegg.internal.ui.UIManager;

public class GEAExpressionTask implements Task{
	
	//Task variables
    private final String TASK_TITLE = "Looking for expression profiles";
	
    private String geneId;
    private GEASummaryPanel panel;
    
    public GEAExpressionTask(String geneId){
    	this.geneId = geneId;
    }

    public GEASummaryPanel getPanel(){
    	return panel;
    }
    
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		taskMonitor.setProgress(-1);
		taskMonitor.setTitle(TASK_TITLE);
		taskMonitor.setStatusMessage(TASK_TITLE);
		
		try {
		
			String accession = getUniprotAccession(geneId);
			
			if(accession.equals("")){
				JOptionPane.showMessageDialog(null, "No expression summary for "+geneId);
				return;
			}
			
			GeaApi gApi = new GeaApi();
			gApi.getSummary(accession);
			
			String ensembl = gApi.getEnsemblGeneId();
			
			if(ensembl.equals("")){
				JOptionPane.showMessageDialog(null, "No expression summary for "+geneId);
				return;
			}
				
			
			GEADAS geadas = new GEADAS();
			panel = new GEASummaryPanel(geadas.getSummary(ensembl));
			
			JFrame frame = new JFrame();
        	frame.setContentPane(panel);
        	frame.setSize(new Dimension(750, 500));
			frame.setVisible(true);
			
			UIManager.getInstance().addExpressionFrame(geneId, frame);
		}catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "No expression summary for "+geneId);
		}
	}
    
	private String getUniprotAccession(String acc) throws RemoteException{
		
		Map<String, List<String>> map = KeggService.getInstance().mapIds(KeggService.getInstance().UNIPROT, new String[]{acc});
		List<String> lst = map.get(acc);
		if(lst != null){
			if(lst.size()>0){
				return lst.get(0);
			}
		}
		return "";
	}


	@Override
	public void cancel() {	
	}

}
