.PHONY: bump-major bump-minor backtest-api live-api

bump-major:
	@./bump-version.sh major

bump-minor:
	@./bump-version.sh minor

backtest-api:
	mvn install -DskipTests
	mvn spring-boot:run -pl algotrade4j-backtest-api

live-api:
	mvn install -DskipTests
	mvn spring-boot:run -pl algotrade4j-live-api
