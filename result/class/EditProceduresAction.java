package edu.ncsu.csc.itrust.action;

import java.util.ArrayList;
import java.util.List;
import edu.ncsu.csc.itrust.action.base.EditOfficeVisitBaseAction;
import edu.ncsu.csc.itrust.beans.ProcedureBean;
import edu.ncsu.csc.itrust.dao.DAOFactory;
import edu.ncsu.csc.itrust.dao.mysql.CPTCodesDAO;
import edu.ncsu.csc.itrust.dao.mysql.ProceduresDAO;
import edu.ncsu.csc.itrust.exception.DBException;
import edu.ncsu.csc.itrust.exception.iTrustException;

/**
 * Edit patiens procedure action
 * @author student
 * @aurhor student
 */
public class EditProceduresAction extends EditOfficeVisitBaseAction {

	private ProceduresDAO proceduresDAO;
	private CPTCodesDAO cptDAO;
	
	/**
	 * @param factory
	 * @param hcpid
	 * @param pidString
	 * @param ovIDString
	 * @throws iTrustException
	 */
	public EditProceduresAction(DAOFactory factory, long hcpid, 
			   	String pidString, String ovIDString) 
		throws iTrustException {
		super(factory, hcpid, pidString, ovIDString);
		proceduresDAO = factory.getProceduresDAO();
		cptDAO = factory.getCPTCodesDAO();
	}
	
	public EditProceduresAction(DAOFactory factory, long hcpid, 
				String pidString) 
		throws iTrustException {
		super(factory, hcpid, pidString);
		proceduresDAO = factory.getProceduresDAO();
		cptDAO = factory.getCPTCodesDAO();
		
	}
	
	public List<ProcedureBean> getProcedures() throws DBException {
		if (isUnsaved()) {
			return new ArrayList<ProcedureBean>();
		} else {
			return proceduresDAO.getMedProceduresList(getOvID());
		}
	}
	
	public void addProcedure(ProcedureBean bean) throws iTrustException {
		verifySaved();
		proceduresDAO.add(bean);
	}
	
	public void editProcedure(ProcedureBean bean) throws iTrustException {
		verifySaved();
		proceduresDAO.edit(bean);
		
	}
	
	public void deleteProcedure(ProcedureBean bean) throws iTrustException {
		verifySaved();
		proceduresDAO.remove(bean.getOvProcedureID());
		
	}
	
	public List<ProcedureBean> getProcedureCodes() throws DBException {
		return cptDAO.getProcedureCPTCodes();
	}

	
}
