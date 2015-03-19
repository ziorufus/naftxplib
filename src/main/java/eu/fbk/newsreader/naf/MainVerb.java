package eu.fbk.newsreader.naf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import ixa.kaflib.*;

public class MainVerb {
	
	public static List<String> detect_main_verbs(KAFDocument nafFile){
		List<String> termIdMainVerb = new ArrayList<String> ();
		
		List<Dep> depL = nafFile.getDeps();
		List<WF> wfL = nafFile.getWFs();
		HashMap<String,String> wfIdL = get_words_by_id(wfL);
		List<List<String>> wfIdBySentL = get_list_words_by_sentence(wfL);
		
		List<Term> termL = nafFile.getTerms();
		HashMap<String,String> wfTermL = get_list_word_term(termL);
		HashMap<String,List<String>> termWfL = get_list_term_word (termL);
		
		List<List<String>> termsBySent = get_terms_by_sentence(wfIdBySentL,wfTermL);
		HashMap<String,Integer> termIdSent = get_list_term_id_sentence (wfIdBySentL, wfTermL);
	
		HashMap<Integer,List<Dep>> depBySentL = get_list_dep_by_sentence (depL, termsBySent, termIdSent);
		
		for (int i : depBySentL.keySet()){
			List<String> listTo = new ArrayList<String>();
			List<String> listFrom = new ArrayList<String> ();
			for (Dep dep : depBySentL.get(i)){
				listTo.add(dep.getTo().getId());
				if (!listFrom.contains(dep.getFrom().getId())){
					listFrom.add(dep.getFrom().getId());
				}
			}

			String mainWord = "";
			String idTerm = "";
			for (String idFrom : listFrom){
				if (!listTo.contains(idFrom)){
				    //List<String> wordL = termWfL.get(idFrom);
				    //for (String word : wordL){
				    //	mainWord += wfIdL.get(word)+" ";
				    //}
				    idTerm = idFrom;
				    //if (getSmallestChunk(idTerm,nafFile)[0].endsWith("VP")){
				    termIdMainVerb.add(idTerm);
				    //}
				}
			}
			//System.out.println("main word in sentence "+i+": "+mainWord+" --> "+idTerm);
		}
		return termIdMainVerb;
	}
	
	
	private static HashMap<Integer,List<Dep>> get_list_dep_by_sentence 
		(List<Dep> depL, List<List<String>> termsBySent, HashMap<String,Integer> termIdSent){
		HashMap<Integer,List<Dep>> depBySent = new HashMap<Integer,List<Dep>> ();
		for (Dep dep : depL){
			int idSent = termIdSent.get(dep.getFrom().getId());
			if (!depBySent.containsKey(idSent)){
				depBySent.put(idSent, new ArrayList<Dep>());
			}
			depBySent.get(idSent).add(dep);
		}
		return depBySent;
	}

	
	private static HashMap<String,Integer> get_list_term_id_sentence (List<List<String>> wfIdBySentL, HashMap<String,String> wfTermL){
		HashMap<String,Integer> termIdSent = new HashMap<String,Integer> ();
		for (int j=0; j<wfIdBySentL.size();j++){
			for (String wfId : wfIdBySentL.get(j)){
				termIdSent.put(wfTermL.get(wfId), j);
			}
		}
		return termIdSent;
	}
	
	private static List<List<String>> get_terms_by_sentence(List<List<String>> wfIdBySentL, HashMap<String,String> wfTermL){
		List<List<String>> termsBySent = new ArrayList<List<String>> ();
		for (int j=0; j<wfIdBySentL.size();j++){
			List<String> temp = new ArrayList<String> ();
			for (String wfId : wfIdBySentL.get(j)){
				temp.add(wfTermL.get(wfId));
			}
			termsBySent.add(temp);
		}
		return termsBySent;
	}
	
	private static HashMap<String,String> get_list_word_term (List<Term> termL){
		HashMap<String,String> wfTermL = new HashMap<String,String>();
		for (Term t : termL){
			for (WF w : t.getWFs()){
				wfTermL.put(w.getId(), t.getId());
			}
		}
		return wfTermL;
	}
	
	private static HashMap<String,List<String>> get_list_term_word (List<Term> termL){
		HashMap<String,List<String>> termWfL = new HashMap<String,List<String>>();
		for (Term t : termL){
			List<String> temp = new ArrayList<String> ();
			for (WF w : t.getWFs()){
				temp.add(w.getId());
			}
			termWfL.put(t.getId(), temp);
		}
		return termWfL;
	}

	
	private static List<List<String>> get_list_words_by_sentence (List<WF> wfL){
		List<List<String>> listWfsSent = new ArrayList ();
		for (WF wf : wfL){
			int numSent = wf.getSent();
			if (listWfsSent.size() < numSent){
				List <String> temp = new ArrayList();
				listWfsSent.add(temp);
			}
			listWfsSent.get(numSent-1).add(wf.getId());
		}
		
		return listWfsSent;
	}
	
	private static HashMap<String,String> get_words_by_id(List<WF> wfL){
		HashMap<String,String> wfIdL = new HashMap ();
		for (WF wf : wfL){
			wfIdL.put(wf.getId(), wf.getForm());
		}
		return wfIdL;
	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File f = new File(args[0]);
		KAFDocument nafFile = KAFDocument.createFromFile(f);
		
		ListIterator<WF> tokenList = nafFile.getWFs().listIterator(); 
		
		detect_main_verbs(nafFile);

	}

}
