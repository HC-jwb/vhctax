package hc.fms.api.addon.vhctax.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table (name="vhc_tax_photo")
@Getter
@Setter
@ToString
public class TaxPhoto {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String type;
	@Lob
	private byte [] image;
	@Column(name="created_dt", updatable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;
	private Long taskId;
	
	public String getImageURL() {
		return String.format("/addon/vhctax/photoimg/%d", this.id);
	}
}
