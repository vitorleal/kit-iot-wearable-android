package kit.iot.wearable.ble;

import java.text.MessageFormat;


public class Commands {
    String ACCELEROMETER;
    String ACCELEROMETER_X;
    String ACCELEROMETER_Y;
    String ACCELEROMETER_Z;

    String LED_OFF;

    String LED_RED_HIGH;
    String LED_RED_LOW;

    String LED_GREEN_HIGH;
    String LED_GREEN_LOW ;

    String LED_BLUE_HIGH;
    String LED_BLUE_LOW;

    String TEMPERATURE;

    String LUMINOSITY;

    String MELODY_CHRISTMAS;
    String MELODY_MARIO;
    String MELODY_IMPERIAL;

    /**
     * Default list of Wearable commands
     */
    public Commands() {
        this.ACCELEROMETER   = "#AC0003\n";
        this.ACCELEROMETER_X = "#AC0000\n";
        this.ACCELEROMETER_Y = "#AC0001\n";
        this.ACCELEROMETER_Z = "#AC0002\n";

        this.LED_OFF = "#LL0000\n";

        this.LED_RED_HIGH = "#LR0255\n";
        this.LED_RED_LOW  = "#LR0000\n";

        this.LED_GREEN_HIGH = "#LG0255\n";
        this.LED_GREEN_LOW  = "#LG0000\n";

        this.LED_BLUE_HIGH = "#LB0255\n";
        this.LED_BLUE_LOW  = "#LB0000\n";

        this.TEMPERATURE = "#TE0000\n";

        this.LUMINOSITY = "#LI0000\n";

        this.MELODY_CHRISTMAS = "#PM1234\n";
        this.MELODY_MARIO     = "#PM6789\n";
        this.MELODY_IMPERIAL  = "#PM4567\n";
    }

    /**
     * Return the LED command with a custom value
     *
     * @param color RED, GREEN, BLUE
     * @param val value between 0 and 255
     * @return command
     */
    public String LED_CUSTOM(String color, int val) {
        String cmd;

        switch (color.toLowerCase()) {
            case "red":
                cmd = "R";
                break;

            case "green":
                cmd = "G";
                break;

            case "blue":
                cmd = "B";
                break;

            default:
                cmd = "G";
                break;
        }

        if (val > 255) {
            val = 255;
        }

        if (val < 0) {
            val = 0;
        }

        return MessageFormat.format("#L{0}0{1}\n", cmd, val);
    }
}
