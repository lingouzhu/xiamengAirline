package xiaMengAirline.newBranch.BusinessDomain;

import xiaMengAirline.newBranch.BasicObject.XiaMengAirlineSolution;
import xiaMengAirline.newBranch.BasicObject.Exception.InvaildSolution;

public interface Validator {
	public void validate (XiaMengAirlineSolution aSolution) throws InvaildSolution;

}
