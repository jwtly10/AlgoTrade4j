.PHONY: bump-major bump-minor run-backtest run-live run-frontend deploy-mkdocs

# No major releases for now
# bump-major:
# 	@./bump-version.sh major

bump-minor:
	@./bump-version.sh minor

run-backtest:
	mvn install -DskipTests
	mvn spring-boot:run -pl algotrade4j-backtest-api

run-live:
	mvn install -DskipTests
	mvn spring-boot:run -pl algotrade4j-live-api

run-frontend:
	cd algotrade4j-frontend && npm run dev

deploy-mkdocs:
	mkdocs gh-deploy