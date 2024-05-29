TOIT_SDK := /usr/src/service/toit
TOIT_COMPILE := $(TOIT_SDK)/bin/toit.compile
TOIT_FIRMWARE := $(TOIT_SDK)/tools/firmware

.PHONY: all
all: ota.bin

/usr/src/service/athena/%.snapshot: /usr/src/service/athena/%.toit
	$(TOIT_COMPILE) -w $@ $<

ota.bin: /usr/src/service/athena/athena.snapshot /usr/src/service/athena/validate.snapshot
	$(TOIT_FIRMWARE) -e $(NEW_FIRMWARE_LOCATION)/firmware.envelope container install validate /usr/src/service/athena/validate.snapshot
	$(TOIT_FIRMWARE) -e $(NEW_FIRMWARE_LOCATION)/firmware.envelope container install ATHENA /usr/src/service/athena/athena.snapshot
	$(TOIT_FIRMWARE) -e $(NEW_FIRMWARE_LOCATION)/firmware.envelope container install jaguar /usr/src/service/jaguar/jaguar.snapshot
	$(TOIT_FIRMWARE) -e $(NEW_FIRMWARE_LOCATION)/firmware.envelope extract --format=binary -o $(NEW_FIRMWARE_LOCATION)/ota.bin