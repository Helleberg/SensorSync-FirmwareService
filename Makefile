NEW_FIRMWARE := /usr/src/service/toit_firmware
TOIT_SDK := /usr/src/service/toit
TOIT_COMPILE := $(TOIT_SDK)/bin/toit.compile
TOIT_FIRMWARE := $(TOIT_SDK)/tools/firmware

.PHONY: all
all: ota.bin

%.snapshot: /usr/src/service/athena/%.toit
	$(TOIT_COMPILE) -w $@ $<

ota.bin: athena.snapshot
	$(TOIT_FIRMWARE) -e $(NEW_FIRMWARE)/firmware.envelope container install ATHENA $(ATHENA)/athena.snapshot
	$(TOIT_FIRMWARE) -e $(NEW_FIRMWARE)/firmware.envelope extract --format=binary -o $(NEW_FIRMWARE)/ota.bin