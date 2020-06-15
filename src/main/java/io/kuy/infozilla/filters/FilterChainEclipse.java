/**
 * FilterChainEclipse.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.filters;

import io.kuy.infozilla.elements.enumeration.Enumeration;
import io.kuy.infozilla.elements.patch.Patch;
import io.kuy.infozilla.elements.sourcecode.java.CodeRegion;
import io.kuy.infozilla.elements.stacktrace.java.StackTrace;
import io.kuy.infozilla.helpers.RegExHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for runnning the complete filter chain on an eclipse input and gathering the results
 * @author Nicolas Bettenburg
 *
 */
public class FilterChainEclipse implements FilterChain {
	// Private Attributes
	private FilterPatches patchFilter;
	private FilterStackTraceJAVA stacktraceFilter;
	private FilterSourceCodeJAVA sourcecodeFilter;
	private FilterEnumeration enumFilter;
	
	private String inputText = "";
	private String outputText = "";
	
	private List<Patch> patches;
	private List<StackTrace> traces;
	private List<CodeRegion> regions;
	private List<Enumeration> enumerations;
	
	// Constructor runs the experiments
	public FilterChainEclipse(String inputText) {
		patchFilter = new FilterPatches();
		stacktraceFilter = new FilterStackTraceJAVA();
		sourcecodeFilter = new FilterSourceCodeJAVA(FilterChainEclipse.class.getResource("/Java_CodeDB.txt"));
		enumFilter = new FilterEnumeration();
		
		this.inputText = RegExHelper.makeLinuxNewlines(inputText);
		this.outputText = this.inputText;
		
		patches = patchFilter.runFilter(outputText);
		outputText = patchFilter.getOutputText();
		
		traces = stacktraceFilter.runFilter(outputText);
		outputText = stacktraceFilter.getOutputText();
		 
		regions = sourcecodeFilter.runFilter(outputText);
		outputText = sourcecodeFilter.getOutputText();
		 
		enumerations = enumFilter.runFilter(outputText);
		// The output of the filter chain
		outputText = sourcecodeFilter.getOutputText();
	}
	
	public FilterChainEclipse(String inputText, boolean runPatches, boolean runTraces, boolean runSource, boolean runEnums) {
		patchFilter = new FilterPatches();
		stacktraceFilter = new FilterStackTraceJAVA();
		sourcecodeFilter = new FilterSourceCodeJAVA(FilterChainEclipse.class.getResource("/Java_CodeDB.txt"));
		enumFilter = new FilterEnumeration();
		
		
		this.inputText = RegExHelper.makeLinuxNewlines(inputText);
		this.outputText = this.inputText;
		
		if (runPatches) {
			patches = patchFilter.runFilter(outputText);
		} else patches = new ArrayList<Patch>();
		
		outputText = patchFilter.getOutputText();
		
		if (runTraces) {
			System.out.println(runTraces);
			traces = stacktraceFilter.runFilter(outputText);
		} else traces = new ArrayList<StackTrace>();
		
		outputText = stacktraceFilter.getOutputText();
		
		if (runSource) {
			regions = sourcecodeFilter.runFilter(outputText);
		} else regions = new ArrayList<CodeRegion>();
		
		outputText = sourcecodeFilter.getOutputText();
		 
		if (runEnums) {
			enumerations = enumFilter.runFilter(outputText);
		} else enumerations = new ArrayList<Enumeration>();
		
		// The output of the filter chain
		outputText = sourcecodeFilter.getOutputText();
	}

	/**
	 * @return the outputText
	 */
	public String getOutputText() {
		return outputText;
	}

	/**
	 * @return the patches
	 */
	public List<Patch> getPatches() {
		return patches;
	}

	/**
	 * @return the traces
	 */
	public List<StackTrace> getTraces() {
		return traces;
	}

	/**
	 * @return the regions
	 */
	public List<CodeRegion> getRegions() {
		return regions;
	}

	/**
	 * @return the enumerations
	 */
	public List<Enumeration> getEnumerations() {
		return enumerations;
	}

	/**
	 * @param inputText the inputText to set
	 */
	public void setInputText(String inputText) {
		this.inputText = inputText;
	}

	/**
	 * @return the inputText
	 */
	public String getInputText() {
		return inputText;
	}
	
}
