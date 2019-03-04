package hc.fms.api.addon.report.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import hc.fms.api.addon.report.entity.FillDrainStatistics;
import hc.fms.api.addon.report.entity.GenSection;
import hc.fms.api.addon.report.model.ExportableReport;
import hc.fms.api.addon.report.model.fuel.FuelEffRateStatSection;
import hc.fms.api.addon.report.model.fuel.FuelStat;
import hc.fms.api.addon.report.model.fuel.filldrain.FillDrainStatSection;

@Service
public class FileExportService {
	private static String[] fuelReportColumnNames = { "일자", "연료소비량(L)", "운행거리(KM)", "연비(KM/L)" };
	private static String[] fillDrainReportColumnNames = { "ID", "일자", "유형", "시작 연료량(%)", "종료 연료량(%)", "주유/누유량(%)",
			"주유/누유 위치", "연료 사용량(%)", "운행거리(KM)" };
	private static String[] vehicleTaxReportColumnNames = { "Label", "Model", "Plate No.", "Type", "Payment No.",
			"Cost", "Due Date", "Status", "Notification", "Payment Receipt" };

	@SuppressWarnings("unchecked")
	public ByteArrayInputStream exportToExcel(ExportableReport<?> report, Boolean isTax) {
		if (report.getReportGen().getFillDrainReportId() != null && isTax == false) {
			return exportFillingSectionListToExcel((ExportableReport<FillDrainStatSection>) report);
		} else if(report.getReportGen().getFillDrainReportId() == null && isTax == false){
			return exportFuelEffRateSectionListToExcel((ExportableReport<FuelEffRateStatSection>) report);
		} else {
			return exportVehicleTaxReportToExcel((ExportableReport<FuelEffRateStatSection>) report);
		}
	}
	

	private ByteArrayInputStream exportFillingSectionListToExcel(ExportableReport<FillDrainStatSection> report) {
		Cell cell;
		Font font;
		CellStyle style;
		XSSFWorkbook workbook = new XSSFWorkbook();
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			XSSFSheet sheet = null;
			List<GenSection> sectionInfoList = report.getSectionInfoList();
			Map<Long, List<FillDrainStatSection>> sectionMap = report.getSectionDataMap();
			List<FillDrainStatSection> sectionStatList;
			int colCount = fillDrainReportColumnNames.length;
			int rowNum = 0;
			Row row;
			int colOffset = 1;
			CellRangeAddress rangeAddress;
			for (GenSection genSection : sectionInfoList) {
				sectionStatList = sectionMap.get(genSection.getTrackerId());
				for (FillDrainStatSection sectionStat : sectionStatList) {
					sheet = workbook.createSheet(genSection.getHeader());
/////////////////////////// top header /////////////////					
					row = sheet.createRow(0);
					font = workbook.createFont();
					font.setFontName("monospaced");
					font.setColor(IndexedColors.BLACK.getIndex());
					font.setFontHeightInPoints((short) 21);

					cell = row.createCell(colOffset);
					style = workbook.createCellStyle();
					style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
					style.setAlignment(CellStyle.ALIGN_CENTER);
					style.setFont(font);
					cell.setCellStyle(style);
					cell.setCellValue(String.format("주유 판단 기준: %s%% 이상", sectionStat.getPercentMin()));
					rangeAddress = new CellRangeAddress(0, 0, colOffset, colCount - 1 + colOffset);
					// RegionUtil.setBorderTop(CellStyle.BORDER_THIN, rangeAddress,
					// (XSSFSheet)sheet, workbook);
					RegionUtil.setBorderBottom(CellStyle.BORDER_MEDIUM, rangeAddress, (XSSFSheet) sheet, workbook);
					sheet.addMergedRegion(rangeAddress);
/////////////////////////// top header /////////////////					

					rowNum = 1;
					row = sheet.createRow(rowNum++);// header row
					row.setHeightInPoints(20);

					font = workbook.createFont();
					font.setColor(IndexedColors.WHITE.getIndex());
					font.setFontHeightInPoints((short) 11);

					for (int colIdx = 0; colIdx < colCount; colIdx++) {
						style = workbook.createCellStyle();
						style.setAlignment(CellStyle.ALIGN_CENTER);
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setFillForegroundColor(IndexedColors.TEAL.getIndex());
						style.setFillPattern(CellStyle.SOLID_FOREGROUND);
						style.setFont(font);
						style.setAlignment(CellStyle.ALIGN_CENTER);
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

						font.setColor(IndexedColors.WHITE.getIndex());
						if (colIdx == 0) {
							style.setBorderLeft(CellStyle.BORDER_THIN);
							style.setLeftBorderColor(IndexedColors.BLACK.getIndex());

							style.setBorderRight(CellStyle.BORDER_THIN);
							style.setRightBorderColor(IndexedColors.WHITE.getIndex());

						} else if (colIdx == (colCount - 1)) {
							style.setBorderRight(CellStyle.BORDER_THIN);
							style.setRightBorderColor(IndexedColors.BLACK.getIndex());
						} else {
							style.setBorderLeft(CellStyle.BORDER_NONE);
							style.setBorderRight(CellStyle.BORDER_THIN);
							style.setRightBorderColor(IndexedColors.WHITE.getIndex());
						}
						cell = row.createCell(colIdx + colOffset);
						cell.setCellValue(fillDrainReportColumnNames[colIdx]);
						cell.setCellStyle(style);

					}
					Font highlightFont = workbook.createFont();
					highlightFont.setFontName("Monospaced");
					highlightFont.setFontHeightInPoints((short) 11);
					highlightFont.setColor(IndexedColors.RED.getIndex());
					List<FillDrainStatistics> fillingList = sectionStat.getFillingList();
					if (fillingList.size() == 0) {
						row = sheet.createRow(rowNum++);// create data row
						font = workbook.createFont();
						font.setFontName("monospaced");
						font.setColor(IndexedColors.BLACK.getIndex());
						font.setFontHeightInPoints((short) 11);

						cell = row.createCell(0 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_CENTER);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue("가져올 데이터가 없습니다.");
						rangeAddress = new CellRangeAddress(rowNum - 1, rowNum - 1, colOffset,
								fillDrainReportColumnNames.length - 1 + colOffset);
						// RegionUtil.setBorderTop(CellStyle.BORDER_THIN, rangeAddress,
						// (XSSFSheet)sheet, workbook);
						RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, rangeAddress, (XSSFSheet) sheet, workbook);
						RegionUtil.setBorderRight(CellStyle.BORDER_THIN, rangeAddress, (XSSFSheet) sheet, workbook);
						RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, rangeAddress, (XSSFSheet) sheet, workbook);
						sheet.addMergedRegion(rangeAddress);
					}
					for (int idx = 0; idx < fillingList.size(); idx++) {
						FillDrainStatistics stat = fillingList.get(idx);
						if (stat.getVolume() < sectionStat.getPercentMin())
							continue;
						row = sheet.createRow(rowNum++);// create data row
						font = workbook.createFont();
						font.setFontName("monospaced");
						font.setColor(IndexedColors.BLACK.getIndex());
						font.setFontHeightInPoints((short) 11);

						cell = row.createCell(0 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_RIGHT);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(stat.getEventId());

						cell = row.createCell(1 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_RIGHT);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(stat.getEventDate());

						cell = row.createCell(2 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_CENTER);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						if (stat.getType().equals(FillDrainStatSection.TYPE_DRAINING)) {
							style.setFont(highlightFont);
						} else {
							style.setFont(font);
						}
						cell.setCellStyle(style);
						cell.setCellValue(stat.getType().equals(FillDrainStatSection.TYPE_DRAINING) ? "누유" : "주유");

						cell = row.createCell(3 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_RIGHT);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(stat.getStartVolume());

						cell = row.createCell(4 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_RIGHT);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(stat.getEndVolume());

						cell = row.createCell(5 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_RIGHT);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(stat.getVolume());

						cell = row.createCell(6 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_LEFT);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(stat.getAddress());

						cell = row.createCell(7 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_RIGHT);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(calculateFuelPctDiff(sectionStat, idx));// stat.getMileageFrom()

						cell = row.createCell(8 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_RIGHT);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(calculateMileageDiff(sectionStat, idx));// stat.getMileageFrom()
					}
				}
				for (int i = 0; i < colCount; i++) {
					if (i == 0)
						sheet.setColumnWidth(i + colOffset, 5 * 256);
					else if (i == 1)
						sheet.setColumnWidth(i + colOffset, 18 * 256);
					else if (i == 6)
						sheet.setColumnWidth(i + colOffset, 50 * 256);
					else
						sheet.setColumnWidth(i + colOffset, 13 * 256);
				}
			}
			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private Double calculateFuelPctDiff(FillDrainStatSection sectionStat, int curIdx) {
		Double diff = null;
		List<FillDrainStatistics> fillingList = sectionStat.getFillingList();
		FillDrainStatistics nextStat, curStat = fillingList.get(curIdx);
		int size = fillingList.size();
		for (int idx = curIdx + 1; idx < size; idx++) {
			nextStat = fillingList.get(idx);
			if (nextStat.getVolume() >= sectionStat.getPercentMin()) {
				diff = curStat.getEndVolume() - nextStat.getStartVolume();
				break;
			}
		}
		return (diff == null) ? (curStat.getEndVolume() - fillingList.get(size - 1).getStartVolume()) : diff;
	}

	private Double calculateMileageDiff(FillDrainStatSection sectionStat, int curIdx) {
		Double diff = null;
		List<FillDrainStatistics> fillingList = sectionStat.getFillingList();
		FillDrainStatistics nextStat, curStat = fillingList.get(curIdx);
		int size = fillingList.size();
		for (int idx = curIdx + 1; idx < size; idx++) {
			nextStat = fillingList.get(idx);
			if (nextStat.getVolume() >= sectionStat.getPercentMin()) {
				diff = nextStat.getMileageFrom() - curStat.getMileageFrom();
				break;
			}
		}
		return (diff == null) ? (fillingList.get(size - 1).getMileageFrom() - curStat.getMileageFrom()) : diff;
	}

	private ByteArrayInputStream exportFuelEffRateSectionListToExcel(ExportableReport<FuelEffRateStatSection> report) {
		Cell cell;
		Font font;
		CellStyle style;
		XSSFWorkbook workbook = new XSSFWorkbook();
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			XSSFSheet sheet = null;
			// ReportGen reportGen = report.getReportGen();
			List<GenSection> sectionInfoList = report.getSectionInfoList();
			Map<Long, List<FuelEffRateStatSection>> sectionMap = report.getSectionDataMap();
			List<FuelEffRateStatSection> sectionStatList;
			int colCount = fuelReportColumnNames.length;
			int rowNum = 0;
			Row row;
			int colOffset = 1;
			for (GenSection genSection : sectionInfoList) {
				sectionStatList = sectionMap.get(genSection.getTrackerId());
				for (FuelEffRateStatSection sectionStat : sectionStatList) {
					sheet = workbook.createSheet(genSection.getHeader());
					rowNum = 1;
					row = sheet.createRow(rowNum++);// header row
					row.setHeightInPoints(20);

					font = workbook.createFont();
					// font.setFontName("Arial");
					font.setColor(IndexedColors.WHITE.getIndex());
					font.setFontHeightInPoints((short) 11);

					for (int colIdx = 0; colIdx < colCount; colIdx++) {
						style = workbook.createCellStyle();
						style.setAlignment(CellStyle.ALIGN_CENTER);
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setFillForegroundColor(IndexedColors.TEAL.getIndex());
						style.setFillPattern(CellStyle.SOLID_FOREGROUND);
						style.setFont(font);
						style.setAlignment(CellStyle.ALIGN_CENTER);
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

						font.setColor(IndexedColors.WHITE.getIndex());
						if (colIdx == 0) {
							style.setBorderLeft(CellStyle.BORDER_THIN);
							style.setLeftBorderColor(IndexedColors.BLACK.getIndex());

							style.setBorderRight(CellStyle.BORDER_THIN);
							style.setRightBorderColor(IndexedColors.WHITE.getIndex());

						} else if (colIdx == (colCount - 1)) {
							style.setBorderRight(CellStyle.BORDER_THIN);
							style.setRightBorderColor(IndexedColors.BLACK.getIndex());
						} else {
							style.setBorderLeft(CellStyle.BORDER_NONE);
							style.setBorderRight(CellStyle.BORDER_THIN);
							style.setRightBorderColor(IndexedColors.WHITE.getIndex());
						}
						cell = row.createCell(colIdx + colOffset);
						cell.setCellValue(fuelReportColumnNames[colIdx]);
						cell.setCellStyle(style);

					}
					for (FuelStat stat : sectionStat.getStatList()) {
						row = sheet.createRow(rowNum++);// create data row
						font = workbook.createFont();
						// font.setFontName("Arial");
						font.setColor(IndexedColors.BLACK.getIndex());
						font.setFontHeightInPoints((short) 11);

						cell = row.createCell(0 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_CENTER);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(stat.getStatDate());

						cell = row.createCell(1 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_RIGHT);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(stat.getFuelUsed());

						cell = row.createCell(2 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_RIGHT);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(stat.getDistanceTravelled());

						cell = row.createCell(3 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_RIGHT);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(stat.getFuelEffRate());
					}
					row = sheet.createRow(rowNum++);// create summary row

					cell = row.createCell(0 + colOffset);
					style = workbook.createCellStyle();
					style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
					style.setAlignment(CellStyle.ALIGN_RIGHT);
					style.setBorderLeft(CellStyle.BORDER_THIN);
					style.setBorderRight(CellStyle.BORDER_THIN);
					style.setBorderTop(CellStyle.BORDER_THIN);
					style.setBorderBottom(CellStyle.BORDER_THIN);
					font = workbook.createFont();

					font.setColor(IndexedColors.BLACK.getIndex());
					font.setFontHeightInPoints((short) 11);
					style.setFont(font);
					cell.setCellStyle(style);
					cell.setCellValue("합 계");

					cell = row.createCell(1 + colOffset);
					style = workbook.createCellStyle();
					style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
					style.setAlignment(CellStyle.ALIGN_RIGHT);
					style.setBorderLeft(CellStyle.BORDER_THIN);
					style.setBorderRight(CellStyle.BORDER_THIN);
					style.setBorderTop(CellStyle.BORDER_THIN);
					style.setBorderBottom(CellStyle.BORDER_THIN);
					font = workbook.createFont();

					font.setColor(IndexedColors.BLACK.getIndex());
					font.setFontHeightInPoints((short) 11);
					style.setFont(font);
					cell.setCellStyle(style);
					cell.setCellValue(sectionStat.getTotalFuelUsed());

					cell = row.createCell(2 + colOffset);
					style = workbook.createCellStyle();
					style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
					style.setAlignment(CellStyle.ALIGN_RIGHT);
					style.setBorderLeft(CellStyle.BORDER_THIN);
					style.setBorderRight(CellStyle.BORDER_THIN);
					style.setBorderTop(CellStyle.BORDER_THIN);
					style.setBorderBottom(CellStyle.BORDER_THIN);
					font = workbook.createFont();

					font.setColor(IndexedColors.BLACK.getIndex());
					font.setFontHeightInPoints((short) 11);
					style.setFont(font);
					cell.setCellStyle(style);
					cell.setCellValue(sectionStat.getTotalDistanceTravelled());

					cell = row.createCell(3 + colOffset);
					style = workbook.createCellStyle();
					style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
					style.setAlignment(CellStyle.ALIGN_RIGHT);
					style.setBorderLeft(CellStyle.BORDER_THIN);
					style.setBorderRight(CellStyle.BORDER_THIN);
					style.setBorderTop(CellStyle.BORDER_THIN);
					style.setBorderBottom(CellStyle.BORDER_THIN);
					font = workbook.createFont();

					font.setColor(IndexedColors.BLACK.getIndex());
					font.setFontHeightInPoints((short) 11);
					style.setFont(font);
					cell.setCellStyle(style);
					cell.setCellValue(sectionStat.getTotalFuelEffRate());
				}
				for (int i = 0; i < colCount; i++) {
					sheet.setColumnWidth(i + colOffset, 15 * 256);
				}
			}
			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private ByteArrayInputStream exportVehicleTaxReportToExcel(ExportableReport<FuelEffRateStatSection> report) {
		System.out.println("in exportVehicleTaxReportToExcel");
		Cell cell;
		Font font;
		CellStyle style;
		XSSFWorkbook workbook = new XSSFWorkbook();
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			XSSFSheet sheet = null;
			// ReportGen reportGen = report.getReportGen();
			List<GenSection> sectionInfoList = report.getSectionInfoList();
			Map<Long, List<FuelEffRateStatSection>> sectionMap = report.getSectionDataMap();
			List<FuelEffRateStatSection> sectionStatList;
			int colCount = vehicleTaxReportColumnNames.length;
			int rowNum = 0;
			Row row;
			int colOffset = 1;
			for (GenSection genSection : sectionInfoList) {
				sectionStatList = sectionMap.get(genSection.getTrackerId());
				for (FuelEffRateStatSection sectionStat : sectionStatList) {
					sheet = workbook.createSheet(genSection.getHeader());
					rowNum = 1;
					row = sheet.createRow(rowNum++);// header row
					row.setHeightInPoints(20);

					font = workbook.createFont();
					// font.setFontName("Arial");
					font.setColor(IndexedColors.WHITE.getIndex());
					font.setFontHeightInPoints((short) 11);

					for (int colIdx = 0; colIdx < colCount; colIdx++) {
						style = workbook.createCellStyle();
						style.setAlignment(CellStyle.ALIGN_CENTER);
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setFillForegroundColor(IndexedColors.TEAL.getIndex());
						style.setFillPattern(CellStyle.SOLID_FOREGROUND);
						style.setFont(font);
						style.setAlignment(CellStyle.ALIGN_CENTER);
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

						font.setColor(IndexedColors.WHITE.getIndex());
						if (colIdx == 0) {
							style.setBorderLeft(CellStyle.BORDER_THIN);
							style.setLeftBorderColor(IndexedColors.BLACK.getIndex());

							style.setBorderRight(CellStyle.BORDER_THIN);
							style.setRightBorderColor(IndexedColors.WHITE.getIndex());

						} else if (colIdx == (colCount - 1)) {
							style.setBorderRight(CellStyle.BORDER_THIN);
							style.setRightBorderColor(IndexedColors.BLACK.getIndex());
						} else {
							style.setBorderLeft(CellStyle.BORDER_NONE);
							style.setBorderRight(CellStyle.BORDER_THIN);
							style.setRightBorderColor(IndexedColors.WHITE.getIndex());
						}
						cell = row.createCell(colIdx + colOffset);
						cell.setCellValue(vehicleTaxReportColumnNames[colIdx]);
						cell.setCellStyle(style);

					}
					for (FuelStat stat : sectionStat.getStatList()) {
						row = sheet.createRow(rowNum++);// create data row
						font = workbook.createFont();
						// font.setFontName("Arial");
						font.setColor(IndexedColors.BLACK.getIndex());
						font.setFontHeightInPoints((short) 11);

						cell = row.createCell(0 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_CENTER);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(stat.getStatDate());

						cell = row.createCell(1 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_RIGHT);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(stat.getFuelUsed());

						cell = row.createCell(2 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_RIGHT);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(stat.getDistanceTravelled());

						cell = row.createCell(3 + colOffset);
						style = workbook.createCellStyle();
						style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
						style.setAlignment(CellStyle.ALIGN_RIGHT);
						style.setBorderLeft(CellStyle.BORDER_THIN);
						style.setBorderRight(CellStyle.BORDER_THIN);
						style.setBorderTop(CellStyle.BORDER_THIN);
						style.setBorderBottom(CellStyle.BORDER_THIN);
						style.setFont(font);
						cell.setCellStyle(style);
						cell.setCellValue(stat.getFuelEffRate());
					}
					row = sheet.createRow(rowNum++);// create summary row

					cell = row.createCell(0 + colOffset);
					style = workbook.createCellStyle();
					style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
					style.setAlignment(CellStyle.ALIGN_RIGHT);
					style.setBorderLeft(CellStyle.BORDER_THIN);
					style.setBorderRight(CellStyle.BORDER_THIN);
					style.setBorderTop(CellStyle.BORDER_THIN);
					style.setBorderBottom(CellStyle.BORDER_THIN);
					font = workbook.createFont();

					font.setColor(IndexedColors.BLACK.getIndex());
					font.setFontHeightInPoints((short) 11);
					style.setFont(font);
					cell.setCellStyle(style);
					cell.setCellValue("합 계");

					cell = row.createCell(1 + colOffset);
					style = workbook.createCellStyle();
					style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
					style.setAlignment(CellStyle.ALIGN_RIGHT);
					style.setBorderLeft(CellStyle.BORDER_THIN);
					style.setBorderRight(CellStyle.BORDER_THIN);
					style.setBorderTop(CellStyle.BORDER_THIN);
					style.setBorderBottom(CellStyle.BORDER_THIN);
					font = workbook.createFont();

					font.setColor(IndexedColors.BLACK.getIndex());
					font.setFontHeightInPoints((short) 11);
					style.setFont(font);
					cell.setCellStyle(style);
					cell.setCellValue(sectionStat.getTotalFuelUsed());

					cell = row.createCell(2 + colOffset);
					style = workbook.createCellStyle();
					style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
					style.setAlignment(CellStyle.ALIGN_RIGHT);
					style.setBorderLeft(CellStyle.BORDER_THIN);
					style.setBorderRight(CellStyle.BORDER_THIN);
					style.setBorderTop(CellStyle.BORDER_THIN);
					style.setBorderBottom(CellStyle.BORDER_THIN);
					font = workbook.createFont();

					font.setColor(IndexedColors.BLACK.getIndex());
					font.setFontHeightInPoints((short) 11);
					style.setFont(font);
					cell.setCellStyle(style);
					cell.setCellValue(sectionStat.getTotalDistanceTravelled());

					cell = row.createCell(3 + colOffset);
					style = workbook.createCellStyle();
					style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
					style.setAlignment(CellStyle.ALIGN_RIGHT);
					style.setBorderLeft(CellStyle.BORDER_THIN);
					style.setBorderRight(CellStyle.BORDER_THIN);
					style.setBorderTop(CellStyle.BORDER_THIN);
					style.setBorderBottom(CellStyle.BORDER_THIN);
					font = workbook.createFont();

					font.setColor(IndexedColors.BLACK.getIndex());
					font.setFontHeightInPoints((short) 11);
					style.setFont(font);
					cell.setCellStyle(style);
					cell.setCellValue(sectionStat.getTotalFuelEffRate());
				}
				for (int i = 0; i < colCount; i++) {
					sheet.setColumnWidth(i + colOffset, 15 * 256);
				}
			}
			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
