@echo off
setlocal EnableExtensions

set "ROOT_DIR=%~dp0.."
pushd "%ROOT_DIR%" >nul

echo ==========================================
echo AI Interview Platform Project Health Check
echo ==========================================
echo.

call :setup_jdk || goto :failed_jdk
echo.

echo [1/6] Checking required files...
call :check_file "README.md" || goto :failed_required
call :check_file "pom.xml" || goto :failed_required
call :check_file "mvnw.cmd" || goto :failed_required
call :check_file "frontend\package.json" || goto :failed_required
call :check_file "sql\init.sql" || goto :failed_required
call :check_file "sql\test-data.sql" || goto :failed_required
echo Required files: OK
echo.

echo [2/6] Checking docs...
call :check_file "docs\PROJECT_SUMMARY.md" || goto :failed_docs
call :check_file "docs\ARCHITECTURE.md" || goto :failed_docs
call :check_file "docs\INTERVIEW_SCRIPT.md" || goto :failed_docs
call :check_file "docs\INTERVIEW_QA.md" || goto :failed_docs
call :check_file "docs\RESUME_DESCRIPTION.md" || goto :failed_docs
call :check_file "docs\FINAL_CHECKLIST.md" || goto :failed_docs
call :check_file "docs\DEMO_READY.md" || goto :failed_docs
call :check_file "docs\DEPLOYMENT_GUIDE.md" || goto :failed_docs
call :check_file "docs\API_OVERVIEW.md" || goto :failed_docs
call :check_file "docs\DEMO_SCRIPT.md" || goto :failed_docs
call :check_file "docs\TEST_REPORT.md" || goto :failed_docs
call :check_file "docs\DELIVERY.md" || goto :failed_docs
echo Docs: OK
echo.

echo [3/6] Compiling backend...
call mvnw.cmd -DskipTests compile
if errorlevel 1 goto :failed_compile
echo Backend compile: OK
echo.

echo [4/6] Running backend tests...
call mvnw.cmd test
if errorlevel 1 goto :failed_tests
echo Backend tests: OK
echo.

echo [5/6] Building frontend...
pushd frontend >nul
call npm.cmd run build
if errorlevel 1 (
  popd >nul
  goto :failed_frontend
)
popd >nul
echo Frontend build: OK
echo.

echo [6/6] Checking git status...
git status --short
if errorlevel 1 (
  echo WARN: git status failed. Please check whether Git is installed and this is a Git repository.
) else (
  for /f "delims=" %%i in ('git status --short') do (
    set "HAS_GIT_CHANGES=1"
  )
  if defined HAS_GIT_CHANGES (
    echo WARN: Working tree has uncommitted changes. Review whether you need git add / commit / push before demo.
  ) else (
    echo Git working tree: clean
  )
)

if exist "target\" echo WARN: target\ exists. It should be ignored by .gitignore and does not need to be committed.
if exist "frontend\node_modules\" echo WARN: frontend\node_modules\ exists. It should be ignored by .gitignore and does not need to be committed.
if exist "frontend\dist\" echo WARN: frontend\dist\ exists. It should be ignored by .gitignore and does not need to be committed.
echo.

echo PROJECT CHECK PASSED
echo Backend compile: OK
echo Backend tests: OK
echo Frontend build: OK
echo Docs: OK

popd >nul
exit /b 0

:setup_jdk
echo Checking JDK...
if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\java.exe" (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    echo JDK: using JAVA_HOME=%JAVA_HOME%
    exit /b 0
  )
  echo WARN: Existing JAVA_HOME is invalid: %JAVA_HOME%
)
call :try_jdk "C:\Program Files\Java\latest\jdk-26" && exit /b 0
call :try_jdk "C:\Users\Whiscjdsb\.jdks\ms-17.0.19" && exit /b 0
echo ERROR: No usable JDK was found.
echo Please install JDK 17+ or set JAVA_HOME to a valid JDK directory.
exit /b 1

:try_jdk
if exist "%~1\bin\java.exe" (
  set "JAVA_HOME=%~1"
  set "PATH=%~1\bin;%PATH%"
  echo JDK: auto detected %~1
  exit /b 0
)
exit /b 1

:check_file
if not exist %~1 (
  echo MISSING: %~1
  exit /b 1
)
echo OK: %~1
exit /b 0

:failed_required
echo.
echo PROJECT CHECK FAILED: required file check failed.
popd >nul
exit /b 1

:failed_docs
echo.
echo PROJECT CHECK FAILED: docs check failed.
popd >nul
exit /b 1

:failed_jdk
echo.
echo PROJECT CHECK FAILED: JDK check failed.
popd >nul
exit /b 1

:failed_compile
echo.
echo PROJECT CHECK FAILED: backend compile failed.
popd >nul
exit /b 1

:failed_tests
echo.
echo PROJECT CHECK FAILED: backend tests failed.
popd >nul
exit /b 1

:failed_frontend
echo.
echo PROJECT CHECK FAILED: frontend build failed.
popd >nul
exit /b 1
