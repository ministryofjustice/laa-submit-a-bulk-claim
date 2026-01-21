{{/* Generate the hostname based on whether externalRoute is enabled */}}
{{- define "ephemeral.hostname" -}}
{{- if and .Values.ingress.externalRoute.hostname (eq .Values.ingress.externalRoute.hostname.enabled true) -}}
{{- .Values.ingress.externalRoute.hostname.value -}}
{{- else -}}
{{- printf "%s-%s-%s.%s" .Release.Name .Chart.Name .Release.Namespace .Values.global.clusterDomain -}}
{{- end -}}
{{- end -}}