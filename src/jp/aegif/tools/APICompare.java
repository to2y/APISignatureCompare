package jp.aegif.tools;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.TypeParameter;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class APICompare {

	public static void main(String[] args) throws Throwable {

		String firstDir = args[0];
		String secondDir = args[1];
		String fileNameListFile = args[2];
		
		APICompare main = new APICompare();
		main.execute(firstDir, secondDir, fileNameListFile);
	
		
//		InputStream in = null;
//		CompilationUnit cu = null;
//		HashMap<String, MethodDeclaration> map = new HashMap<String, MethodDeclaration>();
//		
//		try
//		{
//			in = new FileInputStream(filename);
//			cu = JavaParser.parse(in);
//			MethodVisitor visitor = new MethodVisitor();
//			
//			visitor.visit(cu, map);
//			
//		}
//		catch(ParseException x)
//		{
//			// handle parse exceptions here.
//		}
//		finally
//		{
//			in.close();
//		}

	}
	
	private void execute(String firstDir, String secondDir, String fileNameListFile) throws IOException, ParseException {
		List<String> fileNameList = readFileNameList(fileNameListFile);
		
		for(String fileName : fileNameList) {
			
			HashMap<String, MethodDeclaration> map1 = makeMethodMap(firstDir + "/" + fileName);
			HashMap<String, MethodDeclaration> map2 = makeMethodMap(secondDir + "/" + fileName);
			
			//reduce entry
			reduceMapEntry(map1, map2);
			
			//output diff
			outputDiff(fileName, map1, map2);
		}
		
	}
	private void outputDiff(String fileName, HashMap<String, MethodDeclaration> map1, HashMap<String, MethodDeclaration> map2) {
		
		if ( map1.size() == 0 && map2.size() == 0 ) {
			return;
		}
		System.out.println("======================");
		System.out.println("file: " + fileName);
		System.out.println("= First =");
		for(String key : map1.keySet()) {
			//System.out.println(key);
			MethodDeclaration md = map1.get(key);
			System.out.println(md.getType() + " " + md.getName() + "(" + md.getParameters() + ")");
		}
		System.out.println("= Second =");
		for(String key : map2.keySet()) {
			MethodDeclaration md = map2.get(key);
			System.out.println(md.getType() + " " + md.getName() + "(" + md.getParameters() + ")");
		}	
		
	}
	
	private void reduceMapEntry(HashMap<String, MethodDeclaration> map1, HashMap<String, MethodDeclaration> map2) {
		for(String key : map1.keySet().toArray(new String[0]) ) {
			if ( map2.containsKey(key)) {
				map1.remove(key);
				map2.remove(key);
			}
		}
	}
	
	private List<String> readFileNameList(String fileNameListFile) throws IOException {
		ArrayList<String> fileNameList = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileNameListFile)));
		
		String line;
		while( (line = br.readLine() ) != null ) {
			fileNameList.add(line);
		}
		return fileNameList;
	}
	
	private HashMap<String, MethodDeclaration> makeMethodMap(String filePath) throws IOException, ParseException {
		InputStream in = null;
		CompilationUnit cu = null;
		HashMap<String, MethodDeclaration> map = new HashMap<String, MethodDeclaration>();
		
		try
		{
			in = new FileInputStream(filePath);
			cu = JavaParser.parse(in);
			MethodVisitor visitor = new MethodVisitor();
			
			visitor.visit(cu, map);
			return map;
			
		}
		finally
		{
			in.close();
		}
	}

	public static class MethodVisitor extends VoidVisitorAdapter<HashMap<String, MethodDeclaration>>
	{
		public void visit(MethodDeclaration n, HashMap<String, MethodDeclaration> map)
		{
			if ( n.getModifiers() == ModifierSet.PUBLIC ) {

				StringBuffer sb = new StringBuffer();
				sb.append(n.getName()).append("|");
				sb.append(n.getType()).append("|");

				if ( n.getParameters() != null ) {
					for(Parameter param : n.getParameters()) {
						//					System.out.println(param.getType());
						sb.append(param.getType()).append("|");
					}
				}
				
				map.put(sb.toString(), n);
			}
		}
	}
}
