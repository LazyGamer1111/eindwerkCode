package com.github.lazygamer1111.dataTypes;

public enum ESCCommands {
    DSHOT_CMD_MOTOR_STOP,
    DSHOT_CMD_BEACON1,
    DSHOT_CMD_BEACON2,
    DSHOT_CMD_BEACON3,
    DSHOT_CMD_BEACON4,
    DSHOT_CMD_BEACON5,
    DSHOT_CMD_ESC_INFO, // V2 includes settings
    DSHOT_CMD_SPIN_DIRECTION_1,
    DSHOT_CMD_SPIN_DIRECTION_2,
    DSHOT_CMD_3D_MODE_OFF,
    DSHOT_CMD_3D_MODE_ON,
    DSHOT_CMD_SETTINGS_REQUEST,  	      // Currently not implemented
    DSHOT_CMD_SAVE_SETTINGS,  		      // Need 6x, wait at least 35ms before next command
    DSHOT_CMD_EXTENDED_TELEMETRY_ENABLE,       // Need 6x, wait at least 35ms before next command
    DSHOT_CMD_EXTENDED_TELEMETRY_DISABLE,      // Need 6x, wait at least 35ms before next command
    IGN,IGN1,IGN2,IGN3,IGN4,
    DSHOT_CMD_SPIN_DIRECTION_NORMAL,
    DSHOT_CMD_SPIN_DIRECTION_REVERSED,
    DSHOT_CMD_LED0_ON, // BLHeli32 only
    DSHOT_CMD_LED1_ON, // BLHeli32 only
    DSHOT_CMD_LED2_ON, // BLHeli32 only
    DSHOT_CMD_LED3_ON, // BLHeli32 only
    DSHOT_CMD_LED0_OFF, // BLHeli32 only
    DSHOT_CMD_LED1_OFF, // BLHeli32 only
    DSHOT_CMD_LED2_OFF, // BLHeli32 only
    DSHOT_CMD_LED3_OFF, // BLHeli32 only
    DSHOT_CMD_AUDIO_STREAM_MODE_ON_OFF, // KISS audio Stream mode on/Off
    DSHOT_CMD_SILENT_MODE_ON_OFF, // KISS silent Mode on/Off
    DSHOT_CMD_SIGNAL_LINE_TELEMETRY_DISABLE,
    DSHOT_CMD_SIGNAL_LINE_CONTINUOUS_ERPM_TELEMETRY
}
