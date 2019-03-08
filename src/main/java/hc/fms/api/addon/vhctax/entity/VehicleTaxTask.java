package hc.fms.api.addon.vhctax.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="vhc_tax_task")
@Getter
@Setter
@ToString
public class VehicleTaxTask {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="vhc_id")
	private Long vehicleId;
	private String vin;
	private String label;
	private String model;
	@Column(name="plate_no")
	private String plateNo;
	
	@Column(name="reg_no")
	private String registrationNo;
	private Double cost;
	@Column(name="remind")
	private Integer remindBeforeDays;
	@Column(name="valid_till_dt")
	private String dateValidTill;
	
	@Column(name="noti_sms")
	private String notificationSms;
	@Column(name="noti_email")
	private String notificationEmail;
		
	@Column(name="paid", columnDefinition="TINYINT(1)")
	private boolean paid;
	
	@Column(name="task_type")
	private String taskType;
	
	@Column(name="photo_id")
	private Long photoId;
	@Column(name="rcpt_photo_id")
	private Long receiptPhotoId;
	
	@Transient
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public String getImageURL() {
		return this.photoId == null? "" : String.format("/addon/vhctax/photoimg/%d", this.photoId);
	}
	@Transient
	public String getReceiptImageURL() {
		return this.receiptPhotoId == null? "" : String.format("/addon/vhctax/photoimg/%d", this.receiptPhotoId);
	}
	public Long getDaysLeft() throws ParseException {
		Date today = dateFormat.parse(dateFormat.format(new Date()));
		Date dueDate = dateFormat.parse(this.dateValidTill);
		long diffInMillies = dueDate.getTime() - today.getTime();
	    return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
	}
}
