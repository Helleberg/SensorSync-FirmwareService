TOIT_SDK := /usr/src/service/toit
TOIT_COMPILE := $(TOIT_SDK)/bin/toit.compile
TOIT_FIRMWARE := $(TOIT_SDK)/tools/firmware

.PHONY: all
all: ota.bin

%.snapshot: %.toit
	$(TOIT_COMPILE) -w $@ $<

ota.bin: validate.snapshot
	$(TOIT_FIRMWARE) -e firmware.envelope container install validate validate.snapshot
	$(TOIT_FIRMWARE) -e firmware.envelope extract --format=binary -o ota.bin