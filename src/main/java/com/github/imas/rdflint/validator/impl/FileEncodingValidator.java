package com.github.imas.rdflint.validator.impl;

import com.github.imas.rdflint.LintProblem;
import com.github.imas.rdflint.LintProblemSet;
import com.github.imas.rdflint.utils.StringMatchUtils;
import com.github.imas.rdflint.validator.AbstractRdfValidator;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.mozilla.universalchardet.UniversalDetector;

public class FileEncodingValidator extends AbstractRdfValidator {

  private static final Logger logger = Logger.getLogger(FileEncodingValidator.class.getName());

  UniversalDetector detector = new UniversalDetector();

  private enum EndOfLine {
    CRLF,
    LF,
    CR,
    NONE
  }

  private enum IndentStyle {
    SPACE,
    TAB,
    NONE
  }

  @Override
  public void validateFile(LintProblemSet problems, String path, String parentPath) {
    if (logger.isTraceEnabled()) {
      logger.trace("validateFile: in (path=" + path + ")");
    }
    List<Map<String, String>> params = getValidationParameterMapList();

    String filename = path.substring(parentPath.length() + 1);

    // check parameters
    String charset = null;
    EndOfLine eol = EndOfLine.NONE;
    boolean newline = false;
    IndentStyle indentStyle = IndentStyle.NONE;
    int indentSize = 2;
    boolean trailingSpace = false;
    for (Map<String, String> m : params) {
      if (StringMatchUtils.matchWildcard(path, m.get("target"))) {
        // charset
        if ("utf-8".equals(m.get("charset"))) {
          charset = "UTF-8";
        } else if ("utf-16be".equals(m.get("charset"))) {
          charset = "UTF-16BE";
        } else if ("utf-16le".equals(m.get("charset"))) {
          charset = "UTF-16LE";
        }
        // end_of_line
        if ("cr".equals(m.get("end_of_line"))) {
          eol = EndOfLine.CR;
        } else if ("lf".equals(m.get("end_of_line"))) {
          eol = EndOfLine.LF;
        } else if ("crlf".equals(m.get("end_of_line"))) {
          eol = EndOfLine.CRLF;
        }
        // insert_final_newline
        if ("true".equals(m.get("insert_final_newline"))) {
          newline = true;
        }
        // indent_style
        if ("space".equals(m.get("indent_style"))) {
          indentStyle = IndentStyle.SPACE;
        } else if ("tab".equals(m.get("indent_style"))) {
          indentStyle = IndentStyle.TAB;
        }
        // indent_size
        if (m.get("indent_size") != null) {
          try {
            indentSize = Integer.parseInt(m.get("indent_size"));
          } catch (NumberFormatException ex) {
            // ignore, and default size
          }
        }
        // trim_trailing_whitespace
        if ("true".equals(m.get("trim_trailing_whitespace"))) {
          trailingSpace = true;
        }
      }
    }

    // validation
    String encoding = null;
    try {
      byte[] buf = new byte[4096];
      int nread;
      BufferedInputStream br = new BufferedInputStream(Files.newInputStream(Paths.get(path)));
      while ((nread = br.read(buf)) > 0 && !detector.isDone()) {
        detector.handleData(buf, 0, nread);
      }
      br.close();
      detector.dataEnd();
      encoding = detector.getDetectedCharset();
      detector.reset();

      if (encoding != null && charset != null && !charset.equals(encoding)) {
        problems.addProblem(filename, LintProblem.ErrorLevel.WARN,
            this, "invalidEncoding", charset, encoding);
      }
    } catch (IOException e) {
      e.printStackTrace(); //NOPMD
    }

    try {
      boolean eolResult = true;
      boolean fnlResult = true;
      boolean twsResult = true;
      boolean indResult = true;

      LineInputStream lns = new LineInputStream(
          new BufferedInputStream(Files.newInputStream(Paths.get(path))));
      String lastLn = null;
      String ln;
      while ((ln = lns.readLine()) != null) {
        // end of line
        boolean crlf =
            ln.length() >= 2 && ln.lastIndexOf("\r\n") == (ln.length() - "\r\n".length());
        boolean lf = !crlf && ln.lastIndexOf('\n') == (ln.length() - "\n".length());
        boolean cr = ln.lastIndexOf('\r') == (ln.length() - "\r".length());
        if (eol == EndOfLine.CR && !cr) {
          eolResult = false;
        } else if (eol == EndOfLine.LF && !lf) {
          eolResult = false;
        } else if (eol == EndOfLine.CRLF && !crlf) {
          eolResult = false;
        }

        // trailing white space
        String lnNoEol = ln.replace("\r", "").replace("\n", "");
        if (trailingSpace && lnNoEol.matches(".*\\s$")) {
          twsResult = false;
        }

        // indent
        char indentChar = ' ';
        if (indentStyle == IndentStyle.SPACE) {
          indentChar = ' ';
        } else if (indentStyle == IndentStyle.TAB) {
          indentChar = '\t';
        }
        int cntIndent = 0;
        for (char c : ln.toCharArray()) {
          if (c != ' ' && c != '\t') {
            break;
          }
          if (c != indentChar) {
            indResult = false;
          }
          cntIndent++;
        }
        if (cntIndent % indentSize != 0) {
          indResult = false;
        }

        lastLn = ln;
      }
      // final new line
      if (lastLn != null && lastLn.replace("\r", "").replace("\n", "").equals(lastLn)) {
        fnlResult = false;
      }
      lns.close();

      if (!eolResult) {
        problems.addProblem(filename, LintProblem.ErrorLevel.WARN,
            this, "invalidEol", eol);
      }
      if (newline && !fnlResult && (encoding == null || "UTF-8".equals(encoding))) {
        problems.addProblem(filename, LintProblem.ErrorLevel.WARN,
            this, "needFinalNewLine");
      }
      if (indentStyle != IndentStyle.NONE && !indResult) {
        problems.addProblem(filename, LintProblem.ErrorLevel.WARN,
            this, "invalidIndentSize", indentSize, indentStyle);
      }
      if (!twsResult) {
        problems.addProblem(filename, LintProblem.ErrorLevel.WARN,
            this, "needTrailingWhiteSpace");
      }

    } catch (IOException e) {
      e.printStackTrace(); //NOPMD
    }

    logger.trace("validateFile: out");
  }

  static class LineInputStream {

    private BufferedInputStream stream;
    private StringBuilder buf = new StringBuilder(); // NOPMD

    LineInputStream(BufferedInputStream stream) {
      this.stream = stream;
    }

    String readLine() throws IOException {
      String rtn = null;
      int r;
      while ((r = this.stream.read()) != -1) {
        char c = (char) r;
        buf.append(c);
        if (c == '\n') {
          rtn = buf.toString();
          buf.setLength(0);
          break;
        }
        if (c == '\r') {
          int r2 = this.stream.read();
          if (r2 == '\n') {
            buf.append((char) r2);
            rtn = buf.toString();
            buf.setLength(0);
            break;
          } else {
            rtn = buf.toString();
            buf.setLength(0);
            buf.append((char) r2);
            break;
          }
        }
      }
      if (r == -1 && buf.length() > 0) {
        rtn = buf.toString();
        buf.setLength(0);
      }
      return rtn;
    }

    void close() throws IOException {
      this.stream.close();
      buf = null;
    }
  }


}
