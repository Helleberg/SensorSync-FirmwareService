NEW_FIRMWARE := /usr/src/service/toit_firmware
TOIT_SDK := /usr/src/service/toit
TOIT_COMPILE := $(TOIT_SDK)/bin/toit.compile
TOIT_FIRMWARE := $(TOIT_SDK)/tools/firmware

.PHONY: all
all: ota.bin

/usr/src/service/toit_firmware/%.snapshot: /usr/src/service/athena/%.toit
	$(TOIT_COMPILE) -w $@ $<

ota.bin: /usr/src/service/toit_firmware/athena.snapshot
	$(TOIT_FIRMWARE) -e $(NEW_FIRMWARE)/firmware.envelope container install ATHENA $(NEW_FIRMWARE)/athena.snapshot
	$(TOIT_FIRMWARE) -e $(NEW_FIRMWARE)/firmware.envelope extract --format=binary -o $(NEW_FIRMWARE)/ota.bin