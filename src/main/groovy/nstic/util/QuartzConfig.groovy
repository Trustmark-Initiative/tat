package nstic.util;

import java.util.Properties;

/**
 * Created by brad on 5/7/17.
 */
public class QuartzConfig {

    public static enum TriggerType {
        cron,
        interval;

        public static TriggerType fromSting(String str){
            TriggerType type = null;
            if( str != null && str.length() > 0 ) {
                for (TriggerType t : TriggerType.values()) {
                    if (t.toString().equalsIgnoreCase(str.trim()) ){
                        type = t;
                        break;
                    }
                }
            }
            return type;
        }
    }

    public QuartzConfig(String name){
        this.name = name;
    }

    public QuartzConfig(Properties props, String name){
        this.name = name;
        this.setData(props);
    }

    private String name;

    private TriggerType type;
    private Long startDelay;

    private Long repeatInterval;
    private Integer repeatCount;

    private String cronExpression;


    public void setData(Properties props){
        String triggerType = props.getProperty(this.name+".triggerType");
        String startDelayStr = props.getProperty(this.name+".startDelay");
        String cronExp = props.getProperty(this.name+".cronExpression");
        String repeatCountStr = props.getProperty(this.name+".repeatCount");
        String repeatIntervalStr = props.getProperty(this.name+".repeatInterval");

        this.type = TriggerType.fromSting(triggerType);

        if( startDelayStr != null )
            this.startDelay = Long.parseLong(startDelayStr);
        else
            this.startDelay = 0l;

        if( this.type == TriggerType.cron ){
            this.cronExpression = cronExp;
        }else if( this.type == TriggerType.interval ){
            if( repeatCountStr != null )
                this.repeatCount = Integer.parseInt(repeatCountStr);
            else
                this.repeatCount = -1;

            this.repeatInterval = Long.parseLong(repeatIntervalStr);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TriggerType getType() {
        return type;
    }

    public void setType(TriggerType type) {
        this.type = type;
    }

    public Long getStartDelay() {
        return startDelay;
    }

    public void setStartDelay(Long startDelay) {
        this.startDelay = startDelay;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }


    public String toString() {
        if( this.getType() == TriggerType.cron ){
            return this.getName()+" QuartzConfig: {type:cron, startDelay: "+this.getStartDelay()+", cronExpression: "+this.getCronExpression()+"}";
        }else{
            return this.getName()+" QuartzConfig: {type:interval, startDelay: "+this.getStartDelay()+", repeatInterval: "+this.getRepeatInterval()+", repeatCount: "+this.getRepeatCount()+"}";
        }
    }
}
