package hc.fms.api.addon.report.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import hc.fms.api.addon.vhctax.entity.VehicleTaxTask;

@Service
public class TaxFileExportService {
	private static String[] taxListColumnNames = { "Label", "Model", "Plate No.", "Type", "Payment No.", "Cost",
			"Due Date", "Status" };

	public ByteArrayInputStream exportToExcel(List<VehicleTaxTask> list) {

		Workbook xlsxWb = new XSSFWorkbook();

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		Sheet sheet1 = xlsxWb.createSheet("FirstSheet");

		sheet1.setColumnWidth(0, 10000);
		sheet1.setColumnWidth(9, 10000);

		Row row = null;
		Cell cell = null;

		

		for (int i = 0; i < list.size() + 1; i++) {
			row = sheet1.createRow(i);
			for (int j = 0; j < taxListColumnNames.length; j++) {
				cell = row.createCell(j);
				Font font = xlsxWb.createFont();
				CellStyle style = xlsxWb.createCellStyle();
				style.setAlignment(CellStyle.ALIGN_CENTER);
				style.setAlignment(CellStyle.VERTICAL_CENTER);

				if (i == 0) { // Column Name Setting
					style.setFillForegroundColor(IndexedColors.TEAL.getIndex());
					style.setFillPattern(CellStyle.SOLID_FOREGROUND);
					font.setFontName("monospaced");
					font.setColor(IndexedColors.WHITE.getIndex());
					font.setFontHeightInPoints((short) 11);
					
					cell.setCellValue(taxListColumnNames[j]);
					style.setFont(font);
					cell.setCellStyle(style);

				} else {
					style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
					font.setColor(IndexedColors.BLACK.getIndex());
					switch (j) {
					case 0:
						cell.setCellValue(list.get(i - 1).getLabel());
						break;
					case 1:
						cell.setCellValue(list.get(i - 1).getModel());
						break;
					case 2:
						cell.setCellValue(list.get(i - 1).getPlateNo());
						break;
					case 3:
						cell.setCellValue(list.get(i - 1).getTaskType());
						break;
					case 4:
						cell.setCellValue(list.get(i - 1).getRegistrationNo());
						break;
					case 5:
						cell.setCellValue(list.get(i - 1).getCost().toString());
						break;
					case 6:
						cell.setCellValue(list.get(i - 1).getDateValidTill());
						break;
					case 7:
						cell.setCellValue("");
						break;
					}
				}
			}
		}

		try {
			xlsxWb.write(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ByteArrayInputStream(out.toByteArray());

	}
}
