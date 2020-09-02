package com.withertech.overtok;

import com.withertech.overtok.Components.JColorPane;

import java.io.Writer;

import java.io.IOException;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;


public class JColorPaneWriter extends Writer{



	private boolean closed = false;

	private JColorPane colorPane;

	private StringBuffer buffer;

	/** Constructor.



	 @param colorPane The JColorPane to write to.

	 */

	public JColorPaneWriter(JColorPane colorPane){

		setcolorPane(colorPane);

	}



	/** Set the JColorPane to write to.



	 @param colorPane The JColorPane

	 */



	public void setcolorPane(JColorPane colorPane){

		if(colorPane == null){

			throw new IllegalArgumentException("The text area must not be null.");

		}

		this.colorPane = colorPane;

	}



	/** Close the stream. */

	public void close(){

		closed = true;

	}



	/** Flush the data that is currently in the buffer.



	 @throws IOException

	 */



	public void flush() throws IOException{

		if(closed){

			throw new IOException("The stream is not open.");

		}

		// the newline character should not be necessary.  The PrintWriter

		// should autmatically put the newline, but it doesn't seem to work

		colorPane.appendANSI(getBuffer().toString());

		if(System.getProperty("java.version").startsWith("1.1")){

			append("\n");

		}

		colorPane.setCaretPosition(colorPane.getDocument().getLength());

		buffer = null;

	}

	/** Write the given character array to the output stream.



	 @param charArray The character array

	 @throws IOException

	 */



	public void write(char[] charArray) throws IOException{

		write(charArray, 0, charArray.length);

	}



	/** Write the given character array to the output stream beginning from

	 the given offset and proceeding to until the given length is reached.



	 @param charArray The character array

	 @param offset The start offset

	 @param length The length to write

	 @throws IOException

	 */



	public void write(char[] charArray, int offset, int length) throws IOException{

		if(closed){

			throw new IOException("The stream is not open.");

		}

		getBuffer().append(charArray, offset, length);

	}



	/** Write the given character to the output stream.



	 @param c The character

	 @throws IOException

	 */



	public void write(int c) throws IOException{

		if(closed){

			throw new IOException("The stream is not open.");

		}

		getBuffer().append((char)c);

	}



	/** Write the given String to the output stream.



	 @param string The String

	 @throws IOException

	 */



	public void write(String string) throws IOException{

		if(closed){

			throw new IOException("The stream is not open.");

		}

		getBuffer().append(string);

	}



	/** Write the given String to the output stream beginning from the given offset 

	 and proceeding to until the given length is reached.



	 @param string The String

	 @param offset The start offset

	 @param length The length to write

	 @throws IOException

	 */



	public void write(String string, int offset, int length) throws IOException{

		if(closed){

			throw new IOException("The stream is not open.");

		}

		getBuffer().append(string.substring(offset, length));

	}



	// protected methods



	/** Get the StringBuffer which holds the data prior to writing via

	 a call to the flush() method.  This method should

	 never return null.



	 @return A StringBuffer

	 */



	protected StringBuffer getBuffer(){

		if(buffer == null){

			buffer = new StringBuffer();

		}

		return buffer;

	}

}
