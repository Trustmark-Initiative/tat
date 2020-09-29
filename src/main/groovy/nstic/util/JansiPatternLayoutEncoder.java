package nstic.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.PatternLayoutEncoderBase;
import org.fusesource.jansi.AnsiConsole;

/**
 * This is a custom encoder which supports calling the Jansi render() method on all strings it will write out.  Thus
 * allowing coloring on the output stream.
 * <br/><br/>
 * Created by brad on 3/8/16.
 */
public class JansiPatternLayoutEncoder extends PatternLayoutEncoderBase<ILoggingEvent> {


    @Override
    public void start() {
        JansiPatternLayout patternLayout = new JansiPatternLayout();
        patternLayout.setContext(context);
        patternLayout.setPattern(getPattern());
        patternLayout.setOutputPatternAsHeader(outputPatternAsHeader);
        patternLayout.start();
        this.layout = patternLayout;
        super.start();
    }


    static {
        try{
            AnsiConsole.systemInstall();
        }catch(Throwable t){
            System.err.println("Unable to call AnsiConsole.systemInstall()!");
            t.printStackTrace(System.err);
            System.err.flush();
        }
    }



}//end JansiPatternLayoutEncoder