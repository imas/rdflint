package com.github.imas.rdflint;

import org.jline.reader.EOFError;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;

/**
 * Jline parser for interactive mode.
 */
public class InteractiveParser extends DefaultParser {

  /**
   * Overrided parse method. Need double return to perform command.
   */
  @Override
  public ParsedLine parse(final String line, final int cursor, ParseContext context) {
    ParsedLine pl = super.parse(line, cursor, context);

    if (line.length() == 0) {
      throw new EOFError(-1, -1, "No command", "command");
    }
    if (!line.endsWith("\n") && line.trim().charAt(0) != ':') {
      throw new EOFError(-1, -1, "Single new line", "double newline");
    }
    return pl;
  }

}
