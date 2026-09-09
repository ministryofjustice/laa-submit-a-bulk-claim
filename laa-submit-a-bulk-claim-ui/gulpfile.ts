import gulp, {series, task} from 'gulp';
import rename from 'gulp-rename';
import cleanCSS from 'gulp-clean-css';
import gulpSass from 'gulp-sass';
import * as dartSass from 'sass';
import browserSyncModule from 'browser-sync';
import type {Transform} from 'stream';
import * as esbuild from 'esbuild';
import {execFile} from 'child_process';
import {promisify} from 'util';

const sass = gulpSass(dartSass);
const browserSync = browserSyncModule.create();
const execFileAsync = promisify(execFile);

function compileStylesheets() {
  return gulp.src('src/main/resources/sass/app.scss')
  .pipe(sass.sync({
        // Set to project root and node_modules
        // on purpose to make moj stylesheet find gov uk references
        // properly within node_modules.
        loadPaths: ['.', 'node_modules'],
        quietDeps: true,        // silences the @import deprecation noise from vendor files
        silenceDeprecations: ['import']
      }))
  .pipe(cleanCSS())
  .pipe(rename('app.min.css'))
  .pipe(gulp.dest('build/generatedFEAssets/static/css'))
  .on('end', () => console.log('CSS written to build/generatedFEAssets/static/css'));
}

function copyGOVUKJavascript() {
  return gulp.src('node_modules/govuk-frontend/dist/govuk/govuk-frontend.min.js')
  .pipe(gulp.dest('build/generatedFEAssets/static/js'))
  .pipe(gulp.dest('src/main/resources/es'))
  .on('end', () => console.log('GOV.UK JS copied to build/generatedFEAssets/static/js and src/main/resources/es'));
}

function copyGOVUKAutocompleteJavascript() {
  return gulp.src('node_modules/accessible-autocomplete/dist/accessible-autocomplete.min.js')
  .pipe(gulp.dest('build/generatedFEAssets/static/js'))
  .pipe(gulp.dest('src/main/resources/es'))
  .on('end', () => console.log('GOV.UK Autocomplete JS copied to build/generatedFEAssets/static/js and src/main/resources/es'));

}

function copyMOJJavascript() {
  return gulp.src('node_modules/@ministryofjustice/frontend/moj/moj-frontend.min.js')
  .pipe(gulp.dest('build/generatedFEAssets/static/js'))
  .pipe(gulp.dest('src/main/resources/es'))
  .on('end', () => console.log('MOJ JS copied to build/generatedFEAssets/static/js and src/main/resources/es'));
}

function copyGOVUKAssets() {
  return gulp.src('node_modules/govuk-frontend/dist/govuk/assets/**/*')
  .pipe(gulp.dest('build/generatedFEAssets/static/assets'))
  .on('end', () => console.log('GOV.UK assets copied to build/generatedFEAssets/static/assets'));
}

function copyMOJAssets() {
  return gulp.src('node_modules/@ministryofjustice/frontend/moj/assets/**/*')
  .pipe(gulp.dest('build/generatedFEAssets/static/assets'))
  .on('end', () => console.log('MOJ assets copied to build/generatedFEAssets/static/assets'));
}

async function compileScripts() {
  await esbuild.build({
    entryPoints: ['src/main/resources/es/app.ts'],
    outfile: 'build/generatedFEAssets/static/js/app.min.js',
    bundle: false,
    minify: true,
    target: 'esnext',
    format: 'esm',
  });
  console.log('App JS compiled to build/generatedFEAssets/static/js');
}

async function typeCheck() {
  await execFileAsync('npx', ['tsc', '--noEmit', '-p', 'tsconfig.json']);
}

function watch() {
  browserSync.init({
    proxy: {
      target: 'localhost:8082',
      proxyReq: [
        function (proxyReq: any) {
          // ask the backend not to compress, so BrowserSync can inject the snippet
          proxyReq.setHeader('Accept-Encoding', 'identity');
        }
      ],
      proxyRes: [
        function (proxyRes: any) {
          // strip CSP so BrowserSync's inline client script is allowed (DEV ONLY)
          delete proxyRes.headers['content-security-policy'];
          delete proxyRes.headers['content-security-policy-report-only'];
        }
      ]
    },
    open: true,
    notify: false
  });

  gulp.watch([
    'src/main/resources/templates/**',
    'src/main/resources/templates/**/*'
  ])
  .on('change', (path) => {
    console.log('Template changed:', path);
    browserSync.reload();
  });

  gulp.watch('src/main/resources/sass/**/*.scss', series(compileStylesheets))
  .on('change', (path) => {
    console.log('Stylesheet changed:', path);
    browserSync.reload();
  });

  gulp.watch('src/main/resources/es/**/*.{ts,js}', series(typeCheck, compileScripts))
  .on('change', (path) => {
    console.log('Script changed:', path);
    browserSync.reload();
  });

}

task('type-check', typeCheck);
task('copy-assets', series(copyGOVUKAssets, copyMOJAssets));
task('copy-js', series(copyGOVUKJavascript, copyGOVUKAutocompleteJavascript, copyMOJJavascript));
task('compile-stylesheets', compileStylesheets);
task('compile-scripts', series('copy-js', compileScripts));

// Note: 'copy-js' must run before 'type-check' — app.ts imports the sibling
// govuk-frontend.min.js/moj-frontend.min.js files that copy-js places in es/,
// and tsc will fail to resolve those modules on a fresh checkout otherwise.
task('default', series('copy-js', 'type-check', 'copy-assets', 'compile-stylesheets', 'compile-scripts'));
task('watch', series(watch));
