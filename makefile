.PHONY: run clean watch stop insert get

stop:
	./gradlew --stop


watch:
	./gradlew compileJava --continuous --parallel --build-cache --configuration-cache

run:
	./gradlew bootRun


clean:
	./gradlew clean --refresh-dependencies

insert:
	./scripts/insert.sh

get:
	./scripts/get.sh

