package hc.fms.api.addon.report.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="gen_section")
@IdClass(GenSection.GenSectionId.class)
@Getter
@Setter
@ToString
public class GenSection {
	@Id
	private Long reportId;
	@Id
	private Long trackerId;
	private String header;
	@Getter
	@Setter
	@ToString
	public static class GenSectionId implements java.io.Serializable {

		private static final long serialVersionUID = 2422139602197821240L;
		private Long reportId;
		private Long trackerId;
	}
}
