
/*
 * File State.java
 *
 * Copyright (C) 2010 Remco Bouckaert remco@cs.auckland.ac.nz
 *
 * This file is part of BEAST2.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 *  BEAST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */
package beast.core;

import beast.evolution.tree.Tree;

import java.util.List;
import java.util.ArrayList;

@Description("The state represents the current point in the state space, and " +
		"maintains values of a set of parameters and trees.")
public class State extends Plugin {
	public static final int IS_CLEAN = 0, IS_DIRTY = 1, IS_GORED = 2;

	public Input<List<Parameter>> m_pParameters = new Input<List<Parameter>>("parameter", "parameter, part of the state", new ArrayList<Parameter>());
	public Input<List<Tree>> m_pTrees = new Input<List<Tree>>("tree", "beast.tree, part of the state", new ArrayList<Tree>());

	/** the two components of the state: beast.tree & parameters **/
    public Tree [] m_trees;
    Parameter[] m_parameters = null;

	@Override
	public void initAndValidate(State state) {
		m_trees = m_pTrees.get().toArray(new Tree[0]);
		m_parameters = m_pParameters.get().toArray(new Parameter[0]);
		for (Parameter param : m_parameters) {
			param.m_nParamNr = getParameterIndex(param.getID());
		}
	}


    /** primitive operations on the list of parameters **/
    public void addParameter(Parameter p) {
    	if (m_parameters == null) {
    		m_parameters = new Parameter[1];
    		m_parameters[0] = p;
    		return;
    	}
    	Parameter [] h = new Parameter[m_parameters.length + 1];
    	for (int i = 0; i < h.length - 1; i++) {
    		h[i] = m_parameters[i];
    	}
    	h[h.length-1] = p;
    	m_parameters = h;
    }
    /** return a value with identifier sID. This assumes a single dimensional parameter. **/
    public int getParameterIndex(String sID) {
            for (int i = 0;i < m_parameters.length; i++) {
                    if (m_parameters[i].hasID(sID)) {
                            return i;
                    }
            }
            return -1;
            //throw new Exception("Error 124: No such id (" + sID + ") in parameters");
    }
    public int isDirty(Input<Parameter> p) {return m_parameters[p.get().getParamNr(this)].isDirty();}
    public Double getValue(Input<Parameter> p) {return getValue(p.get());}
    public Double getValue(Parameter p) {return (Double) getValue(p.getParamNr(this));}

    public Object getValue(int nID) {
           return m_parameters[nID].getValue();
    }
    public Object getValue(int nID, int iDim) {
           return m_parameters[nID].getValue(iDim);
    }
    public int isDirty(int nID) {
            return m_parameters[nID].isDirty();
    }
    public Parameter getParameter(int nID) {
            return m_parameters[nID];
    }
    public Parameter getParameter(String sID) {
    	int nID = getParameterIndex(sID);
        return m_parameters[nID];
}
    public Parameter getParameter(Input<Parameter> p) {
    	int nID = p.get().getParamNr(this);
        return (Parameter) m_parameters[nID];
    }
//    public void setValue(int nID, Object fValue) {
//	        m_parameters[nID].setValue(fValue);
//	}
//	void setValue(int nID, int iDim, Object fValue) {
//	        m_parameters[nID].setValue(iDim, fValue);
//	}
  /** multiply a value by a given amount **/
  public void mulValue(double fValue, int m_nParamID) {
          ((Parameter)m_parameters[m_nParamID]).m_values[0] *= fValue;
          m_parameters[m_nParamID].m_bIsDirty = State.IS_DIRTY;
  }
  public void mulValue(int iParam, double fValue, Parameter param) {
	  param.m_values[iParam] *= fValue;
	  param.m_bIsDirty = State.IS_DIRTY;
  }
  public void mulValues(double fValue, Parameter param) {
	  double[] values= param.m_values;
      for (int i = 0;i < values.length; i++) {
                  values[i] *= fValue;
          }
      param.m_bIsDirty = State.IS_DIRTY;
  }

	public State copy() throws Exception {
		State copy = new State();
		copy.m_parameters = new Parameter[m_parameters.length];
		for (int i = 0; i < m_parameters.length; i++) {
			copy.m_parameters[i] = m_parameters[i].copy();
		}
		copy.m_trees = new Tree[m_trees.length];
		for (int i = 0; i < m_trees.length; i++) {
			copy.m_trees[i] = m_trees[i].copy();
		}
		return copy;
	}

    public void addTree(Tree tree) {
    	/** if this is the first beast.tree, create array **/
    	if (m_trees == null) {
    		m_trees = new Tree[1];
    		m_trees[0] = tree;
    		return;
    	}

    	/** resize trees array **/
    	Tree [] h = new Tree[m_trees.length + 1];
    	for (int i = 0; i < h.length - 1; i++) {
    		h[i] = m_trees[i];
    	}
    	h[h.length-1] = tree;
    	m_trees = h;
    }
    public Tree getTree(int nID) {
    	return m_trees[nID];
    }
    public Tree getTree(String sID) {
    	int nID = getTreeIndex(sID);
    	return m_trees[nID];
    }
    public Tree getTree(Input<Tree> p) {
    	return getTree(p.get().getID());
    }
    public int getTreeIndex(String sID) {
    	for (int i = 0; i < m_trees.length; i++) {
    		if (m_trees[i].getID().equals(sID)) {
    			return i;
    		}
    	}
    	return -1;
    }


	public void prepare() throws Exception {
		for (int i = 0; i < m_parameters.length; i++) {
			m_parameters[i].prepare();
		}
	}


	public String toString(List<String> sTaxaNames) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < m_trees.length; i++) {
			buf.append(m_trees[i].getRoot().toString());
			buf.append("\n");
			buf.append(m_trees[i].getRoot().toNewick(sTaxaNames));
			buf.append("\n");
		}
		for (int i = 0; i < m_parameters.length; i++) {
			buf.append(m_parameters[i].toString());
			buf.append("\n");
		}
		return buf.toString();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < m_trees.length; i++) {
			buf.append(m_trees[i].getRoot().toString());
			buf.append("\n");
		}
		for (int i = 0; i < m_parameters.length; i++) {
			buf.append(m_parameters[i].toString());
			buf.append("\n");
		}
		return buf.toString();
	}

	/**
	 * Make sure that state is still consistent
	 * For debugging purposes only
	 * @throws Exception
	 */
	public void validate() throws Exception {
		for (Tree tree : m_trees) {
			tree.validate();
		}
	}

	/** set dirtiness to all parameters and trees **/
	public void makeDirty(int nDirt) {
		for (Parameter param: m_parameters) {
			param.makeDirty(nDirt);
		}
		for (Tree tree : m_trees) {
			tree.makeDirty(nDirt);
		}
	}
}
