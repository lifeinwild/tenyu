package bei7473p5254d69jcuat.tenyu.ui;

import java.io.*;
import java.util.concurrent.locks.*;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.*;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.*;

import glb.*;
import javafx.application.*;
import javafx.scene.control.*;

/**
 * TextAreaAppender for Log4j 2
 */
@Plugin(name = "TextAreaAppender", category = "Core", elementType = "appender", printObject = true)
public final class TextAreaAppender extends AbstractAppender {
	private static TextArea textArea;

	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Lock readLock = rwLock.readLock();

	protected TextAreaAppender(String name, Filter filter,
			Layout<? extends Serializable> layout,
			final boolean ignoreExceptions) {
		super(name, filter, layout, ignoreExceptions);
	}

	/**
	 * This method is where the appender does the work.
	 *
	 * @param event Log event with log data
	 */
	@Override
	public void append(LogEvent event) {
		if (!Platform.isFxApplicationThread()) {
			return;
		}
		readLock.lock();

		final String message = new String(getLayout().toByteArray(event));

		// append log text to TextArea
		try {
			Platform.runLater(() -> {
				try {
					if (textArea != null) {
						String t = textArea.getText();
						if (t.length() == 0) {
							textArea.setText(message);
						} else {
							//delete old log
							String[] lines = t.split("\r\n|\r|\n");
							if(lines.length > 100) {
								StringBuilder sb = new StringBuilder();
								for(int i=50;i<100;i++)
									sb.append(lines[i] + System.lineSeparator());
								textArea.setText(sb.toString());
							}

							textArea.selectEnd();
							textArea.insertText(textArea.getText().length(),
									message);
						}
					}
				} catch (final Throwable t) {
					Glb.getLogger().error("Error while append to TextArea: "
							+ t.getMessage());
				}
			});
		} catch (final IllegalStateException ex) {
			ex.printStackTrace();

		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Factory method. Log4j will parse the configuration and call this factory
	 * method to construct the appender with
	 * the configured attributes.
	 *
	 * @param name   Name of appender
	 * @param layout Log layout of appender
	 * @param filter Filter for appender
	 * @return The TextAreaAppender
	 */
	@PluginFactory
	public static TextAreaAppender createAppender(
			@PluginAttribute("name") String name,
			@PluginElement("Layout") Layout<? extends Serializable> layout,
			@PluginElement("Filter") final Filter filter) {
		if (name == null) {
			LOGGER.error("No name provided for TextAreaAppender");
			return null;
		}
		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		return new TextAreaAppender(name, filter, layout, true);
	}

	/**
	 * Set TextArea to append
	 *
	 * @param textArea TextArea to append
	 */
	public static void setTextArea(TextArea textArea) {
		TextAreaAppender.textArea = textArea;
	}
}