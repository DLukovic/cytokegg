/**
 * Copyright 2013 Jos� Mar�a Villaveces Max Planck institute for biology of
 * ageing (MPI-age)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package age.mpi.de.cytokegg.internal.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.http.HTTPException;

import age.mpi.de.cytokegg.internal.util.Item;

public class KeggService {
	
	private static KeggService instance = new KeggService();
	private String baseUrl = "http://rest.kegg.jp/";
	public final String LIST = "list", GET = "get", INFO = "info", FIND = "find", CONV = "conv", LINK = "link";
	public final String NCBI_GENE_ID = "ncbi-geneid", UNIPROT = "uniprot", NCBI_GI = "ncbi-gi";
	private Item[] organisms;
	
	
	/**
	 * Constructor, private because of singleton
	 */
	private KeggService() {}

	/**
	 * Get the current instance
	 * @return KeggService
	 */
	public static KeggService getInstance() {
		return instance;
	}
	
	public List<Item> getGenesByPathway(String path){
		List<Item> genes = new ArrayList<Item>();
		String[] arguments = new String[]{GET, path};
		String url = addArguments(baseUrl, arguments);
		try {
			HttpURLConnection conn = openConnection(url);
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String line = "";
			boolean analize = false;
			while ((line = br.readLine()) != null){
				if(line.startsWith("GENE")){
					analize = true;
					line = line.replace("GENE", "");
				}
				
				
				/*if(!Character.isDigit(line.trim().charAt(0))){
					analize = false;
				}*/
				
				if(!line.startsWith(" "))
					analize = false;
				
				if(analize){
					String[] geneArr = line.substring(0,line.indexOf(";")).replace("GENE", "").trim().split("  ");
					if(geneArr.length>=2)
						genes.add(new Item(geneArr[0], geneArr[1]));
					
				}
	        }
			br.close();
			closeConnection(conn);
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		return genes;
	}
	
	public Map<String, List<String>> mapIds(String[] targets, String[] genes){
		Map<String,List<String>> mappedGenes = new HashMap<String,List<String>>();
		for(String target : targets){
			Map<String, List<String>> map = mapIds(target, genes);
			for(String key : map.keySet()){
				if(mappedGenes.containsKey(key)){
					List<String> mainLst = mappedGenes.get(key);
					List<String> lst = map.get(key);
					for(String s : lst){
						if(!mainLst.contains(s))
							mainLst.add(s);
					}
    			}else{
    				mappedGenes.put(key, map.get(key));
    			}
			}
		}
		return mappedGenes;
	}
	
	public Map<String, List<String>> mapIds(String target, String[] genes){
		Map<String,List<String>> mapedGenes = new HashMap<String,List<String>>();
		
		String query = "";
		for(String gene : genes)
			query += gene + "+";
		
		String[] arguments = new String[]{CONV, target};
		String url = addArguments(baseUrl, arguments) + query;
		
		try {
			HttpURLConnection conn = openConnection(url);
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String line = "";
			while ((line = br.readLine()) != null){
				String[] lineArr = line.split("\t");
				
				if(mapedGenes.containsKey(lineArr[0])){
    				mapedGenes.get(lineArr[0]).add(lineArr[1]);
    			}else{
    				List<String> lst = new ArrayList<String>();
    				lst.add(lineArr[1]);
    				mapedGenes.put(lineArr[0], lst);
    			}
	        }
			br.close();
			closeConnection(conn);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mapedGenes;
	}
	
	public List<Item> getPathwaysByOrg(String org){
		String[] arguments = new String[]{LIST, "pathway", org};
		String url = addArguments(baseUrl, arguments);
		try {
			List<Item> pathways = new ArrayList<Item>();
			HttpURLConnection conn = openConnection(url);
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String line = "";
			while ((line = br.readLine()) != null){
				String[] lineArr = line.split("\t");
				pathways.add(new Item(lineArr[0], lineArr[1]));
	        }
			br.close();
			closeConnection(conn);
			
			return pathways;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<Item>();
	}
	
	public Item[] getOrganisms(){
		
		if(organisms == null){
			String[] arguments = new String[]{LIST, "organism"};
			String url = addArguments(baseUrl, arguments);
			try {
				List<Item> orgs = new ArrayList<Item>();
				HttpURLConnection conn = openConnection(url);
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
				String line = "";
				while ((line = br.readLine()) != null){
					String[] lineArr = line.split("\t");
					orgs.add(new Item(lineArr[1], lineArr[2]));
		        }
				br.close();
				closeConnection(conn);
				
				int i = 0;
				Item[] orgsArr = new Item[orgs.size()];
				for(Item it : orgs){
					orgsArr[i] = it;
					i++;
				}
				organisms = orgsArr;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return organisms;
	}
	
	private HttpURLConnection openConnection(String url){
		HttpURLConnection huc = null;
		
		try {
			URL u = new URL(url); 
			huc =  (HttpURLConnection)  u.openConnection();
			huc.setRequestMethod("GET"); 
			huc.connect();
			    
			if(huc.getResponseCode() != 200){
				throw new HTTPException(huc.getResponseCode());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return huc;
	}
	
	private void closeConnection(HttpURLConnection conn){
		conn.disconnect();
		conn = null;
	}
	
	private String addArguments(String url, String[] arguments){
		for(String arg : arguments)
			url = addArgument(url, arg);
		return url;
	}
	
	private String addArgument(String url, String argument){
		return url+argument+"/";
	}
	
	public static void main(String[] args){
		KeggService.getInstance().getGenesByPathway("cel00010");
		KeggService.getInstance().getGenesByPathway("hsa04210");
	}
}
