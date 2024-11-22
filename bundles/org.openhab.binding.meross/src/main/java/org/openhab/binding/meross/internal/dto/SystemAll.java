/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.meross.internal.dto;

import java.util.List;

/**
 * The {@link CloudCredentials} class is a pojo holding cloud System All components
 *
 * @author Giovanni Fabiani - Initial contribution
 */
public class SystemAll {

    private Header header;

    public Header getHeader() {
        return header;
    }

    public Payload getPayload() {
        return payload;
    }

    private Payload payload;

    public static class Header {
        private String messageId;
        private String namespace;
        private String triggerSrc;
        private String method;
        private int payloadVersion;
        private String from;
        private String uuid;
        private long timestamp;
        private int timestampMs;
        private String sign;

        public String getMessageId() {
            return messageId;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getTriggerSrc() {
            return triggerSrc;
        }

        public String getMethod() {
            return method;
        }

        public int getPayloadVersion() {
            return payloadVersion;
        }

        public String getFrom() {
            return from;
        }

        public String getUuid() {
            return uuid;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public int getTimestampMs() {
            return timestampMs;
        }

        public String getSign() {
            return sign;
        }
    }

    public static class Payload {
        public All getAll() {
            return all;
        }

        private All all;

        public static class All {
            private System system;
            private Digest digest;

            // Getters
            public System getSystem() {
                return system;
            }

            public Digest getDigest() {
                return digest;
            }

            public static class System {
                private Hardware hardware;
                private Firmware firmware;
                private Time time;
                private Online online;

                // Getters
                public Hardware getHardware() {
                    return hardware;
                }

                public Firmware getFirmware() {
                    return firmware;
                }

                public Time getTime() {
                    return time;
                }

                public Online getOnline() {
                    return online;
                }

                public static class Hardware {
                    private String type;
                    private String subType;
                    private String version;
                    private String chipType;
                    private String uuid;
                    private String macAddress;

                    // Getters
                    public String getType() {
                        return type;
                    }

                    public String getSubType() {
                        return subType;
                    }

                    public String getVersion() {
                        return version;
                    }

                    public String getChipType() {
                        return chipType;
                    }

                    public String getUuid() {
                        return uuid;
                    }

                    public String getMacAddress() {
                        return macAddress;
                    }
                }

                public static class Firmware {
                    private String version;
                    private String homekitVersion;
                    private String compileTime;
                    private int encrypt;
                    private String wifiMac;
                    private String innerIp;
                    private String server;
                    private int port;
                    private int userId;

                    // Getters
                    public String getVersion() {
                        return version;
                    }

                    public String getHomekitVersion() {
                        return homekitVersion;
                    }

                    public String getCompileTime() {
                        return compileTime;
                    }

                    public int getEncrypt() {
                        return encrypt;
                    }

                    public String getWifiMac() {
                        return wifiMac;
                    }

                    public String getInnerIp() {
                        return innerIp;
                    }

                    public String getServer() {
                        return server;
                    }

                    public int getPort() {
                        return port;
                    }

                    public int getUserId() {
                        return userId;
                    }
                }

                public static class Time {
                    private long timestamp;
                    private String timezone;
                    private List<List<Long>> timeRule;

                    // Getters
                    public long getTimestamp() {
                        return timestamp;
                    }

                    public String getTimezone() {
                        return timezone;
                    }

                    public List<List<Long>> getTimeRule() {
                        return timeRule;
                    }
                }

                public static class Online {
                    private int status;
                    private String bindId;
                    private int who;

                    // Getters
                    public int getStatus() {
                        return status;
                    }

                    public String getBindId() {
                        return bindId;
                    }

                    public int getWho() {
                        return who;
                    }
                }
            }

            public static class Digest {
                private List<Togglex> togglex;
                private List<Object> triggerx;
                private List<Object> timerx;

                // Getters
                public List<Togglex> getTogglex() {
                    return togglex;
                }

                public List<Object> getTriggerx() {
                    return triggerx;
                }

                public List<Object> getTimerx() {
                    return timerx;
                }

                public static class Togglex {
                    private int channel;
                    private int onoff;
                    private long lmTime;

                    // Getters
                    public int getChannel() {
                        return channel;
                    }

                    public int getOnoff() {
                        return onoff;
                    }

                    public long getLmTime() {
                        return lmTime;
                    }
                }
            }
        }
    }
}
