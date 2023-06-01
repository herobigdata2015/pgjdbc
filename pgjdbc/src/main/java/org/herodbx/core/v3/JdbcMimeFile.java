package org.herodbx.core.v3;

import org.herodbsql.core.PGStream;

import java.io.*;

// modified:dfh
public class JdbcMimeFile {

	public enum file_mode{
		TELLYOU_FILE_UPLOAD,
		TELLYOU_FILE_CREATE,
		TELLYOU_FILE_READ_OPEN,
		TELLYOU_FILE_READ,
		TELLYOU_FILE_WRITE,
		TELLYOU_FILE_CLOSE,
		TELLYOU_CACHE_COMMAND
	}

	private static void ReportError(PGStream pgStream, String errorMsg) throws IOException {
		errorMsg += "\0";
		byte[] parts = errorMsg.getBytes("UTF-8");
		pgStream.sendChar('e');
		pgStream.sendInteger4(parts.length + 4);
		pgStream.send(parts, parts.length);
		pgStream.flush();
	}

	private static void ReportSuccess(PGStream pgStream) throws IOException {
		pgStream.sendChar('d');
		pgStream.sendInteger4(4);
		pgStream.flush();
	}

	private static void UploadFile(PGStream pgStream, String filename) throws IOException {
		File handle = new File(filename);

		try {
			FileInputStream inputStream = new FileInputStream(handle);
			byte[] buf = new byte[64 * 1024];
			int readsize;

			ReportSuccess(pgStream);
			while ( (readsize = inputStream.read(buf, 0, 64 * 1024)) > 0) {
				pgStream.sendChar('d');
				pgStream.sendInteger4(readsize+4);
				pgStream.send(buf, readsize);
			}
			ReportSuccess(pgStream);
			inputStream.close();
		} catch (Exception e) {
			ReportError(pgStream, "Failure upload file : " + filename);
		}

	}

	private static void OpenFile(PGStream pgStream, String filename, file_mode mode) throws IOException {
		File handle = new File(filename);

		try {
			switch (mode) {
			case TELLYOU_FILE_CREATE:
				pgStream.setOutputStream(new FileOutputStream(handle));
				break;
			case TELLYOU_FILE_READ_OPEN:
				pgStream.setInStream(new FileInputStream(handle));
				break;
			default:
				break;
			}
			pgStream.setMode(mode.ordinal());
			ReportSuccess(pgStream);
		} catch (Exception e) {
			ReportError(pgStream, "Failure open file : " + filename);
		}

	}

	private static void ReadFile(PGStream pgStream, int size) throws IOException {
    byte[] buf = new byte[size];
    InputStream inputStream = pgStream.getInStream();

    size = inputStream.read(buf, 0, size);
    pgStream.sendChar('d');
    pgStream.sendInteger4(size + 4);
    pgStream.send(buf, size);
    pgStream.flush();
  }

	private static void WriteFile(PGStream pgStream, int size) throws IOException {
    byte[] buf = new byte[size];
    OutputStream outputStream = pgStream.getOutputStream();

    pgStream.receive(buf, 0, size);
    outputStream.write(buf);
  }

	private static void CloseFile(PGStream pgStream) throws IOException {
		file_mode mode = file_mode.values()[pgStream.getMode()];

		switch (mode) {
		case TELLYOU_FILE_CREATE: {
      OutputStream outStream = pgStream.getOutputStream();
      if (outStream != null) {
        outStream.close();
      }
      pgStream.setOutputStream(null);
    }
			break;
		case TELLYOU_FILE_READ_OPEN: {
      InputStream inputStream = pgStream.getInStream();
      if (inputStream != null) {
        inputStream.close();
      }
      pgStream.setInStream(null);
    }
			break;

		default:
			break;
		}
	}

	private static void SendCacheCommand(PGStream pgStream, int size) throws IOException {
		byte[] buf = new byte[size];

		pgStream.receive(buf, 0, size);
		pgStream.send(buf, size);
		pgStream.flush();
	}


	public static void JdbcFileOperation(PGStream pgStream) throws IOException {
		file_mode mode;
		int size = 0;
		int filepathenc = 0;
		String filename = "";
		int imode = pgStream.receiveChar();

		mode = file_mode.values()[imode];
		switch (mode) {
		case TELLYOU_FILE_UPLOAD:
		case TELLYOU_FILE_CREATE:
		case TELLYOU_FILE_READ_OPEN:
			filepathenc = pgStream.receiveInteger4();
			size = pgStream.receiveInteger4();
			filename = pgStream.receiveString(size);
			break;
		case TELLYOU_FILE_READ:
		case TELLYOU_FILE_WRITE:
		case TELLYOU_CACHE_COMMAND:
			size = pgStream.receiveInteger4();
			break;
		default:
			break;
		}

		switch (mode) {
		case TELLYOU_FILE_UPLOAD:
			UploadFile(pgStream, filename);
			break;
		case TELLYOU_FILE_CREATE:
		case TELLYOU_FILE_READ_OPEN:
			OpenFile(pgStream, filename, mode);
			break;
		case TELLYOU_FILE_READ:
			ReadFile(pgStream, size);
			break;
		case TELLYOU_FILE_WRITE:
			WriteFile(pgStream, size);
			break;
		case TELLYOU_FILE_CLOSE:
			CloseFile(pgStream);
			break;
		case TELLYOU_CACHE_COMMAND:
			SendCacheCommand(pgStream, size);
			break;

		default:
			break;
		}
	}
}
