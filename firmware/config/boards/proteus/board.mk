# List of all the board related files.
BOARDCPPSRC =  $(PROJECT_DIR)/config/boards/proteus/board_configuration.cpp \
				$(PROJECT_DIR)/config/boards/proteus/adc_hack.cpp

BOARDINC = $(PROJECT_DIR)/config/boards/proteus

ifeq ($(PROJECT_CPU),ARCH_STM32F4)
  IS_STM32F429 = yes
endif

DDEFS += -DEFI_USE_OSC=TRUE
DDEFS += -DLED_CRITICAL_ERROR_BRAIN_PIN=GPIOE_3
DDEFS += -DFIRMWARE_ID=\"proteus\"

DDEFS += -DEFI_MAIN_RELAY_CONTROL=TRUE

DDEFS += -DEFI_ICU_INPUTS=FALSE -DHAL_TRIGGER_USE_PAL=TRUE
DDEFS += -DEFI_LOGIC_ANALYZER=FALSE

DDEFS += -DEFI_CAN_SERIAL=TRUE

# Turn off stuff proteus doesn't have/need
DDEFS += -DEFI_CJ125=FALSE -DEFI_MAX_31855=FALSE -DBOARD_L9779_COUNT=0 -DBOARD_TLE8888_COUNT=0 -DEFI_HD44780_LCD=FALSE -DEFI_LCD=FALSE 

# This stuff doesn't work on H7 yet
ifneq ($(PROJECT_CPU),ARCH_STM32H7)
	DDEFS += -DSTM32_ADC_USE_ADC3=TRUE
	DDEFS += -DEFI_SOFTWARE_KNOCK=TRUE
endif

# disable hardware serial ports on H7
ifeq ($(PROJECT_CPU),ARCH_STM32F4)
	# Hardware serial port on UART 2 -> PD5/PD6
	DDEFS += -DSTM32_UART_USE_USART2=TRUE
	DDEFS += -DTS_PRIMARY_PORT=UARTD2
	DDEFS += -DEFI_CONSOLE_TX_BRAIN_PIN=GPIOD_5 -DEFI_CONSOLE_RX_BRAIN_PIN=GPIOD_6
endif

# We are running on Proteus hardware!
DDEFS += -DHW_PROTEUS=1

# Proteus <=v0.2 needs ADC hack - vbatt is on ADC3
ifeq ($(PROTEUS_LEGACY),TRUE)
	DDEFS +=  -DUSE_ADC3_VBATT_HACK
endif
