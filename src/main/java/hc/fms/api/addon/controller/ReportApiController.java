package hc.fms.api.addon.controller;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hc.fms.api.addon.model.ResponseContainer;
import hc.fms.api.addon.model.ResponseStatus;
import hc.fms.api.addon.properties.FmsProperties;
import hc.fms.api.addon.report.entity.FillDrainStatistics;
import hc.fms.api.addon.report.entity.FuelStatResult;
import hc.fms.api.addon.report.entity.GenSection;
import hc.fms.api.addon.report.entity.ReportGen;
import hc.fms.api.addon.report.model.ExportableReport;
import hc.fms.api.addon.report.model.GroupResponse;
import hc.fms.api.addon.report.model.ReportGenFlatRequest;
import hc.fms.api.addon.report.model.ReportGenResponse;
import hc.fms.api.addon.report.model.ReportResponse;
import hc.fms.api.addon.report.model.SensorResponse;
import hc.fms.api.addon.report.model.TrackerResponse;
import hc.fms.api.addon.report.model.fuel.FuelEffRateStatSection;
import hc.fms.api.addon.report.model.fuel.FuelMileageSection;
import hc.fms.api.addon.report.model.fuel.FuelStat;
import hc.fms.api.addon.report.model.fuel.filldrain.FillDrainStatSection;
import hc.fms.api.addon.report.model.tracker.Tracker;
import hc.fms.api.addon.report.model.tracker.TrackerSensor;
import hc.fms.api.addon.report.service.FileExportService;
import hc.fms.api.addon.report.service.TrackerService;
import hc.fms.api.addon.report.util.HttpUtil;
@RestController
@CrossOrigin("*")
@RequestMapping("/addon/report/api")
public class ReportApiController {
	@Autowired
	private TrackerService trackerService;
	@Autowired
	private FileExportService exportService;
	@Autowired
	private FmsProperties fmsProps;
	private Logger logger = LoggerFactory.getLogger(ReportApiController.class);

	@RequestMapping("/tracker/list")
	public TrackerResponse getTrackers(@RequestBody Long groupId,HttpSession session) {
		TrackerResponse response = trackerService.getTrackerList(HttpUtil.hashKey(session));
		if(response.getSuccess()) {
			List<Tracker> filteredList = response.getList().stream().filter(tracker -> tracker.getGroupId().equals(groupId)).collect(Collectors.toList());
			response.setList(filteredList);
		}
		return response;
	}
	@RequestMapping("/tracker/group/list")
	public GroupResponse getGroupList(HttpSession session) {
		GroupResponse response = trackerService.getGroupList(HttpUtil.hashKey(session));
		return response;
	}
	@RequestMapping("/tracker/sensor/{trackerId}/list")
	public SensorResponse getSensorList(@PathVariable("trackerId") int trackerId, HttpSession session) {
		SensorResponse response = trackerService.getSensorList(HttpUtil.hashKey(session), trackerId);
		return response;
	}
	@PostMapping("/generate")
	public ResponseContainer<ReportGen> requestReportGen(@RequestBody ReportGenFlatRequest req, HttpSession session) {
		/*
		 * session Key: d222e37acb3d15cd0da78c229a7b9d70
		{
			"trackers": [{"trackerId": 78, "mileageSensorId": 656, "fuelSensorId": 640}, {"trackerId": 72, "mileageSensorId": 655, "fuelSensorId": 644}],
			"from":"2018-11-01 00:00:00",
			"to":"2018-11-26 23:59:59",
			"intervalInMin": 360,
			"label": "01-레이벤-45하8608(2017) & 02-모닝-45하9940(2016) 누적운행거리"
		}
		*/

		req.setHash(HttpUtil.hashKey(session));
		req.setClientId(clientId(session));
		logger.info(String.format("requestFuelEffRateReportGen:: session Key %s, clientId: %s",req.getHash(), req.getClientId()));
		
		req.normalize();//add HH:mm:ss to from and to, modify end Date by changing to previous date and to 59:59:59
		req.getTrackers().forEach(info -> {
			SensorResponse sensorResponse = trackerService.getSensorList(req.getHash(), info.getTrackerId());
			if(sensorResponse.getSuccess()) {
				List<TrackerSensor> sensorList = sensorResponse.getList();
				String sensorName;
				for(TrackerSensor sensor : sensorList) {
					sensorName = sensor.getName().replaceAll(" ", "");
/*					
					if(sensorName.equalsIgnoreCase("fuel_consumption")) {
						info.setFuelConsumptionSensorId(sensor.getId());
					} else if(sensorName.equalsIgnoreCase("hardware_mileage")) {
						info.setHardwareMileageSensorId(sensor.getId());
					}
*/
					if(sensorName.equalsIgnoreCase(fmsProps.getApi().getAccumulatedFuelConsumptionLabel().trim())) {
						info.setFuelConsumptionSensorId(sensor.getId());
					} else if(sensorName.equalsIgnoreCase(fmsProps.getApi().getAccumulatedHardwareMileageLabel().trim())) {
						info.setHardwareMileageSensorId(sensor.getId());
					}
				}
			} else {
				logger.info(String.format("Failed to load Sensor List for trackerId: %d %s", info.getTrackerId(),sensorResponse));
				throw new RuntimeException(sensorResponse.getStatus().getDescription());
			}
		});
		logger.info(req.toString());
		ReportGenResponse genResponse = trackerService.generateFuelMileageReport(req);
		
		ResponseContainer<ReportGen> response = new ResponseContainer<>();
		try {
			if(genResponse.isSuccess()) {
				response.setPayload(trackerService.getReportGen(genResponse.getId()));
				response.setSuccess(true);
			} else {
				response.setStatus(genResponse.getStatus());
			}
		} catch(Exception e) {
			ResponseStatus status = new ResponseStatus();
			status.setDescription(e.getMessage());
			response.setStatus(status);
		}
		return response;
	}
	@PostMapping("/generatefilldrain")
	public ResponseContainer<ReportGen> requestFillDrainReportGen(@RequestBody ReportGenFlatRequest req, HttpSession session) {
		/*
		 * session Key: d222e37acb3d15cd0da78c229a7b9d70
		{
			"trackers": [78,72],
			"from":"2018-11-01 00:00:00",
			"to":"2018-11-26 23:59:59",
			"label": "01-레이벤-45하8608(2017) & 02-모닝-45하9940(2016) 누적운행거리"
		}
		*/
		req.setHash(HttpUtil.hashKey(session));
		req.setClientId(clientId(session));
		logger.info(String.format("requestFillDrainReportGen::session Key %s, clientId: %s",req.getHash(), req.getClientId()));
		
		req.normalize();//add HH:mm:ss to from and to, modify end Date by changing to previous date and to 59:59:59
		logger.info(req.toString());
		ReportGenResponse genResponse = trackerService.generateFillDrainReport(req);
		
		ResponseContainer<ReportGen> response = new ResponseContainer<>();
		try {
			if(genResponse.isSuccess()) {
				response.setPayload(trackerService.getReportGen(genResponse.getId()));
				response.setSuccess(true);
			} else {
				response.setStatus(genResponse.getStatus());
			}
		} catch(Exception e) {
			ResponseStatus status = new ResponseStatus();
			status.setDescription(e.getMessage());
			response.setStatus(status);
		}
		return response;
	}
	
	@RequestMapping("/stat/section")
	public ResponseContainer<FuelEffRateStatSection> getFuelEffRateSectionByReport(@RequestBody Map<String, Long> sectionData) {
		ResponseContainer<FuelEffRateStatSection> response = new ResponseContainer<>();
		Long reportId = sectionData.get("reportId");
		Long trackerId = sectionData.get("trackerId");
		try {
			List<FuelStatResult> resultList = trackerService.getFuelStatisticsResultListByReportIdAndTrackerId(reportId, trackerId);
			FuelEffRateStatSection stat = new FuelEffRateStatSection();
			List<FuelStat> statList = resultList.stream().map(statResult-> {
				stat.addFuelUsed(statResult.getFuelUsed());
				stat.addDistanceTravelled(statResult.getDistanceTravelled());
				return new FuelStat(statResult);
			}).collect(Collectors.toList());
			
			stat.setStatList(statList);
			response.setPayload(stat);
			response.setSuccess(true);
		} catch(Exception e) {
			ResponseStatus status = new ResponseStatus();
			status.setDescription(e.getMessage());
			response.setStatus(status);
			e.printStackTrace();
		}
		return response;
		
	}
	@RequestMapping("/filldrain/section")
	public ResponseContainer<FillDrainStatSection> getFillDrainSectionByReport(@RequestBody Map<String, Long> sectionData) {
		ResponseContainer<FillDrainStatSection> response = new ResponseContainer<>();
		Long reportId = sectionData.get("reportId");
		Long trackerId = sectionData.get("trackerId");
		try {
			List<FillDrainStatistics> statList = trackerService.getFillDrainStatisticsByReportIdAndTrackerId(reportId, trackerId);
			FillDrainStatSection statSection = new FillDrainStatSection();
			statSection.setStatList(statList);
			statSection.setFillingList(statList.stream().filter(stat -> stat.getType().equals(FillDrainStatSection.TYPE_FILLING)).collect(Collectors.toList()));
			statSection.setDrainingList(statList.stream().filter(stat -> stat.getType().equals(FillDrainStatSection.TYPE_DRAINING)).collect(Collectors.toList()));
			response.setPayload(statSection);
			response.setSuccess(true);
		} catch(Exception e) {
			ResponseStatus status = new ResponseStatus();
			status.setDescription(e.getMessage());
			response.setStatus(status);
			e.printStackTrace();
		}
		return response;
		
	}
	@RequestMapping("/genlist")
	public ResponseContainer<List<ReportGen>> getGeneratedReportList(HttpSession session) {
		ResponseContainer <List<ReportGen>> response = new ResponseContainer<>();
		try {
			response.setPayload(trackerService.getFuelReportGenList(clientId(session)));
			response.setSuccess(true);
		} catch(Exception e) {
			e.printStackTrace();
			ResponseStatus status = new ResponseStatus();
			status.setDescription(e.getMessage());
			response.setStatus(status);
		}
		return response;
	}
	@RequestMapping("/genlist/filldrain")
	public ResponseContainer<List<ReportGen>> getGeneratedFillDrainReportList(HttpSession session) {
		ResponseContainer <List<ReportGen>> response = new ResponseContainer<>();
		try {
			response.setPayload(trackerService.getFillDrainReportGenList(clientId(session)));
			response.setSuccess(true);
		} catch(Exception e) {
			e.printStackTrace();
			ResponseStatus status = new ResponseStatus();
			status.setDescription(e.getMessage());
			response.setStatus(status);
		}
		return response;
	}
	
	@RequestMapping("/genlist/inprogress")
	public  ResponseContainer<List<Long>> getGenListInProgress(HttpSession session) {
		ResponseContainer<List<Long>> response = new ResponseContainer<>();
		try {
			response.setPayload(trackerService.getReportGenListInProgress(clientId(session)));
			response.setSuccess(true);
		} catch(Exception e) {
			e.printStackTrace();
			ResponseStatus status = new ResponseStatus();
			status.setDescription(e.getMessage());
			response.setStatus(status);
		}
		
		return response;
	}
	@RequestMapping("/genfilldrainlist/inprogress")
	public  ResponseContainer<List<Long>> getGenFillDrainListInProgress(HttpSession session) {
		ResponseContainer<List<Long>> response = new ResponseContainer<>();
		try {
			response.setPayload(trackerService.getFillDrainReportGenListInProgress(clientId(session)));
			response.setSuccess(true);
		} catch(Exception e) {
			e.printStackTrace();
			ResponseStatus status = new ResponseStatus();
			status.setDescription(e.getMessage());
			response.setStatus(status);
		}
		
		return response;
	}
	@RequestMapping("/section/list")
	public ResponseContainer<List<GenSection>> getSectionListFor(@RequestBody ReportGen reportGen) {
		ResponseContainer<List<GenSection>> response = new ResponseContainer<>();
		try {
			List<GenSection> sectionList = trackerService.getSectionListFor(reportGen.getId());
			response.setPayload(sectionList);
			response.setSuccess(true);
		} catch(Exception e) {
			ResponseStatus status = new ResponseStatus();
			status.setDescription(e.getMessage());
			response.setStatus(status);
		}
		return response;
	}
	
	/** retrieve original report not processed by this module, this is for testing use*/
	@RequestMapping("/retrieve/source/{sourceId}")
	public ReportResponse<FuelMileageSection> retrieveReportSource(HttpSession session, @PathVariable("sourceId")Long sourceId) {
		return trackerService.retrieveFuelMileageReport(HttpUtil.hashKey(session), sourceId);
	}
	
	@RequestMapping("/xlsdownload/{reportId}")
	public ResponseEntity<InputStreamResource> downloadReportAsExcel(@PathVariable("reportId") Long reportId) {
		ExportableReport<?> reportSource = trackerService.getExportableReport(reportId);
		ReportGen reportGen = reportSource.getReportGen();
		ByteArrayInputStream in = exportService.exportToExcel(reportSource, false);
		HttpHeaders headers = new HttpHeaders();
		String filePrefix = reportGen.getFillDrainReportId() != null? "Filling_Report" : "FuelReport"; 
		headers.add("Content-Disposition",String.format("attachment; filename=%s_%s_%s.xlsx", filePrefix, reportGen.getFrom().substring(0, 10), reportGen.getTo().substring(0, 10)));
		return ResponseEntity
				.ok()
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.headers(headers).body(new InputStreamResource(in));
	}
	
	@RequestMapping("/fmsurl")
	public ResponseContainer<String> getFmsUrl(HttpSession session) {
		ResponseContainer <String> response = new ResponseContainer<>();
		String hashKey = HttpUtil.hashKey(session);
		if(hashKey != null) {
			response.setPayload(String.format("http://%s/pro/demo/?session_key=%s", fmsProps.getSiteAddress(),hashKey));
			response.setSuccess(true);
		} else {
			ResponseStatus status = new ResponseStatus();
			status.setDescription("Hash session key has not been found");
			response.setStatus(status);
		}
		return response;
	}
	
	private String clientId(HttpSession session) {
		return (String)session.getAttribute("clientId");
	}
}
