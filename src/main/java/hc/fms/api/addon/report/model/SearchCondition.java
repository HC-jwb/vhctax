package hc.fms.api.addon.report.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchCondition {
	private String taskType = "";
	private String fromDate;
	private String toDate;
	
	
	public SearchCondition(String taskType, String fromDate, String toDate) {
		this.taskType = taskType;
		this.fromDate = fromDate;
		this.toDate = toDate;
	}
}
