.PHONY: bump-major bump-minor backtest-api live-api

bump-major:
	@./bump-version.sh major

bump-minor:
	@./bump-version.sh minor

run-backtest-api:
	mvn install -DskipTests
	mvn spring-boot:run -pl algotrade4j-backtest-api

run-live-api:
	mvn install -DskipTests
	mvn spring-boot:run -pl algotrade4j-live-api

deploy-mkdocs:
	mkdocs gh-deploy